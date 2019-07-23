package c.jahhow.remotecontroller;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class TouchPadLayout extends FrameLayout {

	MainActivity mainActivity;
	boolean vibrateOnDownOnly;

	public TouchPadLayout(@NonNull Context context) {
		super(context);
	}

	public TouchPadLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchPadLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public TouchPadLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public void Initialize(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		if (mainActivity.vibrator == null) vibrateOnDownOnly = false;
		else
			vibrateOnDownOnly = mainActivity.preferences.getBoolean(MainActivity.KeyPrefer_VibrateOnDown, false);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}


	final float density = getResources().getDisplayMetrics().density;
	float originXdp, originYdp;
	float origin2PointerAverageYdp;
	int maxPointerCount, lastMaxPointerCount;
	boolean hasMoveEventExceedSlop = false;
	boolean downIsInDoubleClickInterval = false;
	boolean upIsFirstOfIntensiveClicks;// of intensive clicks
	long upTime = 0;
	int intensiveClickIntervalMs = 300;

	int lastAction;
	final Object lock = new Object();
	boolean semaphore = false;
	final Object vibrateLock = new Object();
	boolean vibrateMutex;

	float pxSlop = 2 * density;

	class DownEvent {
		float X, Y;
		int ID;

		DownEvent(float x, float y, int id) {
			X = x;
			Y = y;
			ID = id;
		}
	}

	List<DownEvent> downEventList = new ArrayList<>();

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				if (downIsInDoubleClickInterval) {// last down
					if (/*last*/!hasMoveEventExceedSlop && event.getEventTime() - upTime < intensiveClickIntervalMs) {
						switch (maxPointerCount) {// last
							case 1:
								mainActivity.SendMouseLeftDown();
								break;
							case 2:
								mainActivity.SendMouseRightDown();
								break;
						}
						downIsInDoubleClickInterval = true;
					} else {
						downIsInDoubleClickInterval = false;
					}
				} else {
					synchronized (lock) {
						downIsInDoubleClickInterval = semaphore;
						semaphore = false;
					}
					if (downIsInDoubleClickInterval & vibrateOnDownOnly) {
						synchronized (vibrateLock) {
							vibrateMutex = true;
						}
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(intensiveClickIntervalMs);
								} catch (InterruptedException ignored) {
								}
								synchronized (vibrateLock) {
									if (vibrateMutex) {
										vibrateMutex = false;
										mainActivity.Vibrate(1);
									}
								}
							}
						}).start();
					}
				}
				downEventList.add(new DownEvent(event.getX(), event.getY(), event.getPointerId(0)));
				lastMaxPointerCount = maxPointerCount;
				maxPointerCount = 1;
				hasMoveEventExceedSlop = false;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				if (hasMoveEventExceedSlop) {
					if (lastAction == MotionEvent.ACTION_MOVE) {
						if (event.getPointerCount() == 1) {
							float xdp = event.getX() / density;
							float ydp = event.getY() / density;
							float diffXdp = xdp - originXdp;
							float diffYdp = ydp - originYdp;
							float adjFactor = (float) GetAdjustFactor(diffXdp, diffYdp, moveMouseAdjExp);
							int roundAdjDiffXdp = Math.round(adjFactor * diffXdp);
							int roundAdjDiffYdp = Math.round(adjFactor * diffYdp);
							if (roundAdjDiffXdp != 0 || roundAdjDiffYdp != 0) {
								mainActivity.SendMouseMove((short) roundAdjDiffXdp, (short) roundAdjDiffYdp);
								originXdp += roundAdjDiffXdp / adjFactor;
								originYdp += roundAdjDiffYdp / adjFactor;
							}
						} else if (event.getPointerCount() == 2) {
							float averageYdp = (event.getY() + event.getY(1)) / (2 * density);
							float diffYdp = averageYdp - origin2PointerAverageYdp;
							double adjFactor = GetScrollAdjustFactor(diffYdp);
							double adjDiffYdp = adjFactor * diffYdp;
							int roundAdjDiffYdp = (int) Math.round(adjDiffYdp);
							if (roundAdjDiffYdp != 0) {
								mainActivity.SendMouseWheel(roundAdjDiffYdp);
								origin2PointerAverageYdp += roundAdjDiffYdp / adjFactor;
							}
						}
					} else {
						//hasMoveEventExceedSlop = true;
						if (event.getPointerCount() == 2) {
							origin2PointerAverageYdp = (event.getY() + event.getY(1)) / (2 * density);
						} else if (event.getPointerCount() == 1) {
							originXdp = event.getX() / density;
							originYdp = event.getY() / density;
						}
					}
				} else {
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				upTime = event.getEventTime();
				if (downIsInDoubleClickInterval) {
					switch (lastMaxPointerCount) {
						case 1:
							mainActivity.SendMouseLeftUp();
							break;
						case 2:
							mainActivity.SendMouseRightUp();
							break;
					}
					if (upIsFirstOfIntensiveClicks) {//last
						upIsFirstOfIntensiveClicks = false;
						if (!hasMoveEventExceedSlop) {
							boolean shouldSendAClick = false;
							if (vibrateOnDownOnly) {
								synchronized (vibrateLock) {
									if (vibrateMutex) {
										vibrateMutex = false;
										shouldSendAClick = true;
									}
								}
							} else if (upTime - event.getDownTime() < intensiveClickIntervalMs) {
								shouldSendAClick = true;
							}

							if (shouldSendAClick) {
								switch (lastMaxPointerCount) {
									case 1:
										mainActivity.SendMouseLeftClick();
										break;
									case 2:
										mainActivity.SendMouseRightClick();
										break;
								}
							}
						}
					}
				} else {
					upIsFirstOfIntensiveClicks = true;
					if (!hasMoveEventExceedSlop) {
						switch (maxPointerCount) {
							case 1:
								mainActivity.SendMouseLeftDown();
								break;
							case 2:
								mainActivity.SendMouseRightDown();
								break;
						}
						final int curMaxPointerCount = maxPointerCount;
						synchronized (lock) {
							semaphore = true;
						}
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(intensiveClickIntervalMs);
									synchronized (lock) {
										if (semaphore) {
											semaphore = false;
											switch (curMaxPointerCount) {
												case 1:
													mainActivity.SendMouseLeftUp();
													break;
												case 2:
													mainActivity.SendMouseRightUp();
													break;
											}
										}
									}
								} catch (InterruptedException ignored) {
								}
							}
						}).start();
					}
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
				if (downIsInDoubleClickInterval) {
					downIsInDoubleClickInterval = false;
					mainActivity.SendMouseLeftUp();
				}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				int index = event.getActionIndex();
				downEventList.add(new DownEvent(event.getX(index),event.getY(index),event.getPointerId(index)));
				int pointerCount = event.getPointerCount();
				if (maxPointerCount < pointerCount) maxPointerCount = pointerCount;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				break;
		}
		lastAction = action;
		return true;
	}

	double scrollAdjMultiplier = 4;

	double GetScrollAdjustFactor(double dp) {
		return scrollAdjMultiplier * Math.pow(Math.abs(dp), moveMouseAdjExp - 1);
	}

	double moveMouseAdjExp = 1.2;

	static double GetAdjustFactor(double dxDp, double dyDp, double expFactor) {
		return Math.pow(dxDp * dxDp + dyDp * dyDp, expFactor - 1);
	}
}
