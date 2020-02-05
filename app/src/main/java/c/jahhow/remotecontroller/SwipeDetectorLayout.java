package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

@SuppressLint("ViewConstructor")
public class SwipeDetectorLayout extends FrameLayout {

    private final MainActivity mainActivity;
    //int movingChildIndex = 0;
    private SwipeCardView movingChild;
    private final Interpolator interpolator = new DecelerateInterpolator(2);

    public SwipeDetectorLayout(MainActivity mainActivity) {
        super(mainActivity);
        movingChild = new SwipeCardView(mainActivity, interpolator);
        addView(movingChild);
        addView(new SwipeCardView(mainActivity, interpolator));
        addView(new SwipeCardView(mainActivity, interpolator));
        addView(new SwipeCardView(mainActivity, interpolator));
        addView(new SwipeCardView(mainActivity, interpolator));
        indexLastChild = getChildCount() - 1;
        this.mainActivity = mainActivity;
    }

    private int mActivePointerId;
    private float touchOriginX;
    private float touchOriginY;
    private float viewOriginX;
    private float viewOriginY;
    private float diffX;
    private float diffY;
    private final float density = getResources().getDisplayMetrics().density;
    private final float pxSlop = 4 * density;
    //float transYAtAppear = 40 * density;

    private final long duration = 800;

    @Override
    public void addView(final View child) {
        child.setVisibility(getChildCount() == 0 ? VISIBLE : GONE);
        child.animate().setDuration(duration);
        super.addView(child);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mainActivity.preferences.contains(MainActivity.KeyPrefer_Swiped) || mainActivity.preferences.getBoolean(MainActivity.KeyPrefer_SwipeDemo, true))
            Demo();
    }

    boolean demoing;

    private final Runnable readyNextChildRunnable = new Runnable() {
        @Override
        public void run() {
            ReadyNextChild();
        }
    };

    private void Demo() {
        demoing = true;
        final Interpolator demoInterpolator = new AccelerateDecelerateInterpolator();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.i(SwipeDetectorLayout.class.getSimpleName(), "Start DEMO LOOP");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                while (demoing) {
                    movingChild.animate().translationY(-getHeight())
                            .setInterpolator(demoInterpolator)/*.setListener(listener)*/;
                    mainActivity.runOnUiThread(readyNextChildRunnable);

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ignored) {
                    }
                    if (!demoing) break;
                    movingChild.animate().translationY(getHeight())
                            .setInterpolator(demoInterpolator)/*.setListener(listener)*/;
                    mainActivity.runOnUiThread(readyNextChildRunnable);

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ignored) {
                    }
                    if (!demoing) break;
                    movingChild.animate().translationX(-getWidth())
                            .setInterpolator(demoInterpolator)/*.setListener(listener)*/;
                    mainActivity.runOnUiThread(readyNextChildRunnable);

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ignored) {
                    }
                    if (!demoing) break;
                    movingChild.animate().translationX(getWidth())
                            .setInterpolator(demoInterpolator)/*.setListener(listener)*/;
                    mainActivity.runOnUiThread(readyNextChildRunnable);

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {
                    }
                }
                //Log.i(SwipeDetectorLayout.class.getSimpleName(), " End  DEMO LOOP");
            }
        }).start();
    }

    @Override
    protected void onDetachedFromWindow() {
        demoing = false;
        //Log.i(SwipeDetectorLayout.class.getSimpleName(), "onDetachedFromWindow()");
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private boolean ignoreSameMoves = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                demoing = false;
                ignoreSameMoves = true;
                NewActivePointer(event);
                movingChild.animate().setDuration(0).translationXBy(0).translationYBy(0);// Cancel translation animation
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (ignoreSameMoves) {
                    float bacDiffX = diffX, bacDiffY = diffY;
                    computePointerDiff(event);
                    if (bacDiffX != diffX || bacDiffY != diffY) {
                        ignoreSameMoves = false;
                        MigrateTouchPoint(bacDiffX, bacDiffY, diffX, diffY);
                        IndicateDirection();
                    }
                } else {
                    MoveCurChild(event);
                    IndicateDirection();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                computePointerDiff(event);
                ViewPropertyAnimator animator = movingChild.animate();
                float absDiffX = Math.abs(diffX), absDiffY = Math.abs(diffY);
                if (absDiffX > pxSlop | absDiffY > pxSlop) {
                    if (absDiffY > absDiffX) {
                        int transY = getHeight();
                        if (diffY > 0) {
                            mainActivity.SendClick_Up(null);
                        } else {
                            mainActivity.SendClick_Down(null);
                            transY = -transY;
                        }
                        animator.translationY(transY);
                    } else {
                        int transX = getWidth();
                        if (diffX > 0) {
                            mainActivity.SendClick_Left(null);
                        } else {
                            mainActivity.SendClick_Right(null);
                            transX = -transX;
                        }
                        animator.translationX(transX);
                    }
                    animator.setInterpolator(interpolator).setDuration(duration);

                    ReadyNextChild();
                } else {
                    ResetMovingChild();
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
                ResetMovingChild();
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                ChangeActivePointer(event, event.getActionIndex());
                ignoreSameMoves = true;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int activeIndex = event.findPointerIndex(mActivePointerId);
                int index = event.getActionIndex();
                if (activeIndex == index) {
                    ChangeActivePointer(event, index == 0 ? 1 : 0);
                }
                break;
            }
        }
        return true;
    }

    private final int indexLastChild;

    private void ReadyNextChild() {
        //movingChildIndex = (movingChildIndex + 1) % getChildCount();
        movingChild = (SwipeCardView) getChildAt(indexLastChild);
        removeViewAt(indexLastChild);
        addView(movingChild, 0);
        movingChild.Reset(false);
        AnimateShowView(movingChild);
    }

    private void IndicateDirection() {
        float absDiffX = Math.abs(diffX), absDiffY = Math.abs(diffY);
        if (absDiffX > pxSlop | absDiffY > pxSlop) {
            if (absDiffY > absDiffX) {
                if (diffY > 0) {
                    movingChild.Indicate(SwipeCardView.IndicatorUp);
                } else {
                    movingChild.Indicate(SwipeCardView.IndicatorDown);
                }
            } else {
                if (diffX > 0) {
                    movingChild.Indicate(SwipeCardView.IndicatorLeft);
                } else {
                    movingChild.Indicate(SwipeCardView.IndicatorRight);
                }
            }
        } else {
            movingChild.Reset(true);
        }
    }

    private void AnimateShowView(View view) {
        view.animate().cancel();
        view.setAlpha(0);
        view.setScaleX(.8F);
        view.setScaleY(.8F);
        view.setTranslationY(0);
        view.setTranslationX(0);
        view.setVisibility(VISIBLE);
        view.animate().setInterpolator(interpolator).alpha(1).scaleX(1).scaleY(1);
    }

    private void ResetMovingChild() {
        movingChild.Reset(true);
        movingChild.animate().setDuration(duration).translationX(0).translationY(0);
    }

    private void NewActivePointer(MotionEvent event) {
        diffX = diffY = 0;
        touchOriginX = event.getX();
        touchOriginY = event.getY();
        viewOriginX = movingChild.getTranslationX();
        viewOriginY = movingChild.getTranslationY();
        mActivePointerId = event.getPointerId(0);
    }

    private void ChangeActivePointer(MotionEvent event, int newIndex) {
        int oldPointerIndex = event.findPointerIndex(mActivePointerId);
        MigrateTouchPoint(event.getX(oldPointerIndex), event.getY(oldPointerIndex), event.getX(newIndex), event.getY(newIndex));
        mActivePointerId = event.getPointerId(newIndex);
    }

    private void MigrateTouchPoint(float fromX, float fromY, float toX, float toY) {
        touchOriginX += toX - fromX;
        touchOriginY += toY - fromY;
    }

    private void MoveCurChild(MotionEvent event) {
        computePointerDiff(event);
        movingChild.setTranslationX(viewOriginX + diffX);
        movingChild.setTranslationY(viewOriginY + diffY);
    }

    private void computePointerDiff(MotionEvent event) {
        computePointerDiff(event, event.findPointerIndex(mActivePointerId));
    }

    private void computePointerDiff(MotionEvent event, int index) {
        diffX = event.getX(index) - touchOriginX;
        diffY = event.getY(index) - touchOriginY;
    }
}
