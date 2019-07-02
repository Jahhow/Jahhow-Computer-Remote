package c.jahhow.remotecontroller;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchPadLayout extends FrameLayout {

	MainActivity mainActivity;

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
		this.mainActivity=mainActivity;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}


	final float density = getResources().getDisplayMetrics().density;
	float originXdp, originYdp;
	float origin2PointerAverageYdp;
	int maxPointerCount, lastMaxPointerCount;
	boolean hasMove;
	boolean downIsInDoubleClickInterval;
	boolean upIsFirstOfIntensiveClicks;// of intensive clicks
	long upTime = 0;
	int intensiveClickIntervalMs = 300;

	int lastAction;
	final Object lock = new Object();
	boolean available;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				if (downIsInDoubleClickInterval) {// last down
					if (event.getEventTime() - upTime < intensiveClickIntervalMs) {
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
						downIsInDoubleClickInterval = available;
						available = false;
					}
				}
				lastMaxPointerCount = maxPointerCount;
				maxPointerCount = 1;
				hasMove = false;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				if (lastAction == MotionEvent.ACTION_MOVE) {
					if (event.getPointerCount() == 1) {
						float xdp = event.getX() / density;
						float ydp = event.getY() / density;
						int roundDiffXdp = Math.round(xdp - originXdp);
						int roundDiffYdp = Math.round(ydp - originYdp);
						if (roundDiffXdp != 0 || roundDiffYdp != 0) {
							mainActivity.SendMouseMove(AdjustMouseMove(roundDiffXdp), AdjustMouseMove(roundDiffYdp));
							originXdp += roundDiffXdp;
							originYdp += roundDiffYdp;
						}
					} else if (event.getPointerCount() == 2) {
						float averageYdp = (event.getY() + event.getY(1)) / (2 * density);
						int roundDiffYdp = Math.round(averageYdp - origin2PointerAverageYdp);
						mainActivity.SendMouseWheel(AdjustMouseMove(roundDiffYdp));
						origin2PointerAverageYdp += roundDiffYdp;
					}
				} else {
					hasMove = true;
					if (event.getPointerCount() == 2) {
						origin2PointerAverageYdp = (event.getY() + event.getY(1)) / (2 * density);
					} else if (event.getPointerCount() == 1) {
						originXdp = event.getX() / density;
						originYdp = event.getY() / density;
					}
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
						if (upTime - event.getDownTime() < intensiveClickIntervalMs && !hasMove) {
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
				} else {
					upIsFirstOfIntensiveClicks = true;
					if (!hasMove) {
						switch (maxPointerCount) {
							case 1:
								mainActivity.SendMouseLeftDown();
								available = true;
								new Thread(new Runnable() {
									@Override
									public void run() {
										try {
											Thread.sleep(intensiveClickIntervalMs);
											synchronized (lock) {
												if (available) {
													available = false;
													mainActivity.SendMouseLeftUp();
												}
											}
										} catch (InterruptedException ignored) {
										}
									}
								}).start();
								break;
							case 2:
								mainActivity.SendMouseRightClick();
								break;
						}
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
				int pointerCount = event.getPointerCount();
				if (maxPointerCount < pointerCount) maxPointerCount = pointerCount;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				break;
		}
		lastAction = action;
		return true;
	}

	double exp = 1.5;

	short AdjustMouseMove(double dp) {
		if (dp < 0)
			return (short) -Math.pow(-dp, exp);
		return (short) Math.pow(dp, exp);
	}
}
