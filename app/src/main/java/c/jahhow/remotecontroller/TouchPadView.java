package c.jahhow.remotecontroller;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class TouchPadView extends FrameLayout {

    MainActivity mainActivity;
    boolean vibrateOnDownOnly;

    public TouchPadView(@NonNull Context context) {
        super(context);
    }

    public TouchPadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchPadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TouchPadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    //float pxSlop = 0;

    DownEventList downEventList = new DownEventList(/*pxSlop*/);

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                //Log.i(getClass().getSimpleName(), "ACTION_DOWN");
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
                downEventList.clear();
                downEventList.add(new DownEventList.DownEvent(event.getX(), event.getY(), event.getPointerId(0)));
                lastMaxPointerCount = maxPointerCount;
                maxPointerCount = 1;
                hasMoveEventExceedSlop = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                //Log.i(getClass().getSimpleName(), "ACTION_MOVE");
                if (hasMoveEventExceedSlop) {
                    if (lastAction == MotionEvent.ACTION_MOVE) {
                        if (event.getPointerCount() == 1) {
                            float xdp = event.getX() / density;
                            float ydp = event.getY() / density;
                            float diffXdp = xdp - originXdp;
                            float diffYdp = ydp - originYdp;
                            float adjFactor = (float) GetAdjustFactor(diffXdp, diffYdp, moveMouseAdjExp, 1);
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
                            double adjFactor = GetAdjustFactor(0, diffYdp, moveMouseAdjExp, scrollAdjMultiplier);
                            double adjDiffYdp = adjFactor * diffYdp;
                            int roundAdjDiffYdp = (int) Math.round(adjDiffYdp);
                            if (roundAdjDiffYdp != 0) {
                                mainActivity.SendMouseWheel(roundAdjDiffYdp);
                                origin2PointerAverageYdp += roundAdjDiffYdp / adjFactor;
                            }
                        }
                    } else {
                        SetMoveOrigin(event);
                    }
                } else {
                    hasMoveEventExceedSlop = !downEventList.isEqualToMoveEvent(event);
                    if (hasMoveEventExceedSlop) {
                        //Log.i(getClass().getSimpleName(), "hasMoveEventExceedSlop = true");
                        SetMoveOrigin(event);
                    } else {
                        return true;// ignore this event. ignore lastAction = action
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                //Log.i(getClass().getSimpleName(), "ACTION_UP");
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
                downEventList.clear();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                //Log.i(getClass().getSimpleName(), "ACTION_CANCEL");
                if (downIsInDoubleClickInterval) {
                    downIsInDoubleClickInterval = false;
                    mainActivity.SendMouseLeftUp();
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                //Log.i(getClass().getSimpleName(), "ACTION_POINTER_DOWN");
                int index = event.getActionIndex();
                downEventList.add(new DownEventList.DownEvent(event.getX(index), event.getY(index), event.getPointerId(index)));
                int pointerCount = event.getPointerCount();
                if (maxPointerCount < pointerCount) maxPointerCount = pointerCount;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                //Log.i(getClass().getSimpleName(), "ACTION_POINTER_UP");
                int index = event.getActionIndex();
                int id = event.getPointerId(index);
                downEventList.remove(downEventList.findById(id));
                break;
            }
            default: {
                //Log.i(getClass().getSimpleName(), "MotionEvent NOT HANDLED");
            }
        }
        lastAction = action;
        return true;
    }

    private void SetMoveOrigin(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        float Y0 = event.getY();
        if (pointerCount == 2) {
            origin2PointerAverageYdp = (Y0 + event.getY(1)) / (2 * density);
        } else if (pointerCount == 1) {
            originXdp = event.getX() / density;
            originYdp = Y0 / density;
        }
    }

    static double scrollAdjMultiplier = 3;

    double moveMouseAdjExp = 1.2;

    static double GetAdjustFactor(double dxDp, double dyDp, double base, double scale) {
        return scale * Math.pow(base, Math.sqrt(dxDp * dxDp + dyDp * dyDp));
    }


    static class DownEventList extends ArrayList<DownEventList.DownEvent> {
		/*float squarePxSlop;

		DownEventList(float pxSlop) {
			super();
			squarePxSlop = pxSlop * pxSlop;
		}

		boolean isMoveEventExceedSlop(MotionEvent moveEvent) {
			for (DownEvent downEvent : this) {
				int index = moveEvent.findPointerIndex(downEvent.ID);
				float dx = moveEvent.getX(index) - downEvent.X;
				float dy = moveEvent.getY(index) - downEvent.Y;
				Log.i(TouchPadLayout.class.getSimpleName(), String.format("dx:%f dy:%f", dx, dy));
				float dr2 = dx * dx + dy * dy;
				if (dr2 > squarePxSlop)
					return true;
			}
			return false;
		}*/

        static class DownEvent {
            float X, Y;
            int ID;

            DownEvent(float x, float y, int id) {
                X = x;
                Y = y;
                ID = id;
            }
        }

        static int NotFound = -1;

        // return list index
        int findById(int id) {
            for (int i = 0; i < size(); ++i) {
                DownEvent downEvent = get(i);
                if (downEvent.ID == id) return i;
            }
            return NotFound;
        }

        boolean isEqualToMoveEvent(MotionEvent moveEvent) {
            for (DownEvent downEvent : this) {
                int index = moveEvent.findPointerIndex(downEvent.ID);
                if (moveEvent.getX(index) != downEvent.X || moveEvent.getY(index) != downEvent.Y)
                    return false;
            }
            return true;
        }
    }
}