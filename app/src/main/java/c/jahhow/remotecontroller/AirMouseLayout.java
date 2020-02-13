package c.jahhow.remotecontroller;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

@SuppressLint("ViewConstructor")
public class AirMouseLayout extends FrameLayout implements ValueAnimator.AnimatorUpdateListener {
    private double scrollBuffer;
    private static final float expSmoothTimeConstant = .2582437027204f;
    private final float scale = 3.0f;
    private float animationDiffX = 0.0f;
    private float animationDiffY = 0.0f;
    private final ExponentialSmoothing smootherY;
    private final ExponentialSmoothing smootherX;
    public int I = 0;
    private final Runnable J = new K(this);
    private final Runnable K = new L(this);
    private boolean L;
    private float M;
    private final MainActivity mainActivity;
    private final AirMouseFragment airMouseFragment;
    public final AirMouseCardView mouseCardView;
    private final ValueAnimator f1704d = new CompatTimeAnimator();
    private final Interpolator decelerateInterpolator = new DecelerateInterpolator(2.0f);
    private final Interpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    private int focusingPointerID;
    private float focusingPointerOriginX;
    private float focusingPointerOriginY;
    private float diffX;
    private float diffY;
    private float startFocusingX;
    private float startFocusingY;
    private float n;
    private float focusingPointerLastY;
    public final float density = getResources().getDisplayMetrics().density;
    private final float q = (this.density * 4.0f);
    private final long r = 800;
    public boolean attachedToWindow;
    private final int t = 300;
    private final int u = 300;
    private boolean preventJump = false;
    public boolean aFocusingPointerActuallyMoved;
    private boolean scroll;
    public boolean y;
    private int maxPointerCount;

    public AirMouseLayout(MainActivity mainActivity, AirMouseFragment airMouseFragment) {
        super(mainActivity);
        float refreshRate = mainActivity.getWindowManager().getDefaultDisplay().getRefreshRate();
        float refreshRateAware_NewValueWeight = (float) (1 - Math.exp(-1 / refreshRate / expSmoothTimeConstant));
        smootherY = new ExponentialSmoothing(refreshRateAware_NewValueWeight);
        smootherX = new ExponentialSmoothing(refreshRateAware_NewValueWeight);

        this.mouseCardView = (AirMouseCardView) mainActivity.getLayoutInflater().inflate(R.layout.air_mouse_card_view, this, false);
        this.mouseCardView.Init(this.accelerateDecelerateInterpolator);
        this.mouseCardView.animate();
        addView(this.mouseCardView);
        setKeepScreenOn(true);
        this.f1704d.addUpdateListener(this);
        this.mainActivity = mainActivity;
        this.airMouseFragment = airMouseFragment;
    }

    private void SwitchOrigin(float x, float y, float newX, float newY) {
        focusingPointerOriginX += (newX - x);
        focusingPointerOriginY += (newY - y);
    }

    private void a(int i2) {
        this.mouseCardView.Indicate(i2);
        this.mouseCardView.animate().setInterpolator(this.decelerateInterpolator).setDuration(this.r).translationX(0.0f).translationY(0.0f);
    }

    private void changeFocusingPointer(MotionEvent motionEvent, int newPointerIndex) {
        this.preventJump = true;
        this.startFocusingX = motionEvent.getX(newPointerIndex);
        this.startFocusingY = motionEvent.getY(newPointerIndex);
        int findPointerIndex = motionEvent.findPointerIndex(this.focusingPointerID);
        if (this.scroll) {
            this.focusingPointerLastY = motionEvent.getY(newPointerIndex);
        } else {
            SwitchOrigin(motionEvent.getX(findPointerIndex), motionEvent.getY(findPointerIndex), motionEvent.getX(newPointerIndex), motionEvent.getY(newPointerIndex));
        }
        this.focusingPointerID = motionEvent.getPointerId(newPointerIndex);
    }

    private void b(MotionEvent motionEvent, int focusingPointerIndex) {
        this.diffX = motionEvent.getX(focusingPointerIndex) - this.focusingPointerOriginX;
        this.diffY = motionEvent.getY(focusingPointerIndex) - this.focusingPointerOriginY;
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (scroll) {
            mouseCardView.setTranslationY(smootherY.Smoothen(animationDiffY * scale));
            this.animationDiffY = 0.0f;
        } else {
            mouseCardView.setTranslationX(smootherX.Smoothen(animationDiffX * scale));
            this.animationDiffX = 0.0f;
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.attachedToWindow = true;
        new Thread(new SwipeCardDemoRunnable(this)).start();
    }

    public void onDetachedFromWindow() {
        this.attachedToWindow = false;
        //Log.i(MotionMouseLayout.class.getSimpleName(), "onDetachedFromWindow()");
        super.onDetachedFromWindow();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return true;
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent motionEvent) {
        AirMouseCardView airMouseCardView;
        int actionIndex;
        int actionMasked = motionEvent.getActionMasked();
        boolean z2 = false;
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            this.attachedToWindow = false;
            this.aFocusingPointerActuallyMoved = false;
            this.y = true;
            this.diffY = 0.0f;
            this.diffX = 0.0f;
            this.focusingPointerOriginX = motionEvent.getX();
            this.focusingPointerOriginY = motionEvent.getY();
            this.mouseCardView.getTranslationX();
            this.mouseCardView.getTranslationY();
            this.focusingPointerID = motionEvent.getPointerId(0);
            this.preventJump = true;
            this.startFocusingX = motionEvent.getX(0);
            this.startFocusingY = motionEvent.getY(0);
            this.mouseCardView.animate().cancel();
            this.maxPointerCount = 1;
            this.airMouseFragment.pauseMouseMove(1);
            postDelayed(this.J, (long) this.t);
        } else {
            int i2 = -1;
            int i3 = 3;
            if (actionMasked == MotionEvent.ACTION_UP) {
                if (this.aFocusingPointerActuallyMoved) {
                    if (!this.scroll) {
                        this.mainActivity.Vibrate();
                        if (this.L) {
                            this.mainActivity.SendMouseLeftUp();
                        } else {
                            this.mainActivity.SendMouseRightUp();
                        }
                    }
                    this.f1704d.cancel();
                } else if (this.maxPointerCount == 1 && motionEvent.getEventTime() - motionEvent.getDownTime() < ((long) this.t)) {
                    this.mainActivity.Vibrate();
                    this.mainActivity.SendMouseLeftClick();
                    this.I++;
                    postDelayed(this.K, (long) this.u);
                    i2 = 3;
                }
                this.airMouseFragment.pauseMouseMove(0);
                a(i2);
                this.y = false;
            } else {
                int i4 = 2;
                if (actionMasked == MotionEvent.ACTION_MOVE) {
                    int focusingPointerIndex = motionEvent.findPointerIndex(this.focusingPointerID);
                    float curFocusingX = motionEvent.getX(focusingPointerIndex);
                    float curFocusingY = motionEvent.getY(focusingPointerIndex);
                    if (this.aFocusingPointerActuallyMoved) {
                        if (this.scroll) {
                            if (this.preventJump) {
                                if (curFocusingY != this.startFocusingY) {
                                    this.focusingPointerLastY = curFocusingY;
                                    this.preventJump = false;
                                }
                            } else {
                                animationDiffY = curFocusingY - this.focusingPointerLastY;
                                double animationDiffYDp = ((double) this.animationDiffY) / this.density;
                                double adjustedDiffYDp = MotionAdjuster.GetDefaultScrollMultiplier(animationDiffYDp) * animationDiffYDp;
                                scrollBuffer += adjustedDiffYDp;
                                int round = (int) Math.round(scrollBuffer);
                                if (round != 0) {
                                    mainActivity.SendMouseWheel(round);
                                    scrollBuffer = 0;
                                }
                                this.focusingPointerLastY = curFocusingY;
                            }
                        } else {
                            if (this.preventJump) {
                                float f2 = this.startFocusingX;
                                if (curFocusingX != f2) {
                                    this.n = curFocusingX;
                                    this.preventJump = false;
                                    SwitchOrigin(f2, this.startFocusingY, curFocusingX, curFocusingY);
                                }
                            } else {
                                this.animationDiffX = curFocusingX - this.n;
                                this.n = curFocusingX;
                                b(motionEvent, focusingPointerIndex);
                                if (this.diffX < this.M) {
                                    z2 = true;
                                }
                                if (this.L ^ z2) {
                                    this.mainActivity.Vibrate();
                                    if (z2) {
                                        this.mainActivity.SendMouseRightUp();
                                        this.mainActivity.SendMouseLeftDown();
                                        airMouseCardView = this.mouseCardView;
                                    } else {
                                        this.mainActivity.SendMouseLeftUp();
                                        this.mainActivity.SendMouseRightDown();
                                        airMouseCardView = this.mouseCardView;
                                        i3 = 1;
                                    }
                                    airMouseCardView.Indicate(i3);
                                    this.M = -this.M;
                                    this.L = z2;
                                }
                            }
                        }
                    } else if (curFocusingX != this.startFocusingX || curFocusingY != this.startFocusingY) {
                        this.preventJump = false;
                        this.aFocusingPointerActuallyMoved = true;
                        b(motionEvent, focusingPointerIndex);
                        float absDiffX = Math.abs(this.diffX);
                        float absDiffY = Math.abs(this.diffY);
                        this.mainActivity.Vibrate();
                        this.scroll = absDiffY > absDiffX;
                        if (this.scroll) {
                            this.focusingPointerLastY = curFocusingY;
                            this.smootherY.setValue(mouseCardView.getTranslationY());
                            this.scrollBuffer = 0.0f;
                        } else {
                            this.n = curFocusingX;
                            this.smootherX.setValue(mouseCardView.getTranslationX());
                            this.airMouseFragment.pauseMouseMove(0);
                            if (this.diffX < 0.0f) {
                                z2 = true;
                            }
                            this.L = z2;
                            if (this.L) {
                                this.mainActivity.SendMouseLeftDown();
                                i4 = 3;
                            } else {
                                this.mainActivity.SendMouseRightDown();
                                i4 = 1;
                            }
                            SwitchOrigin(this.startFocusingX, this.startFocusingY, curFocusingX, curFocusingY);
                            float f3 = this.q;
                            this.M = f3;
                            if (!this.L) {
                                this.M = -f3;
                            }
                        }
                        this.f1704d.start();
                        this.mouseCardView.Indicate(i4);
                    }
                } else if (actionMasked == 3) {
                    a(-1);
                } else if (actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                    changeFocusingPointer(motionEvent, motionEvent.getActionIndex());
                    int pointerCount = motionEvent.getPointerCount();
                    if (pointerCount > this.maxPointerCount) {
                        this.maxPointerCount = pointerCount;
                    }
                } else if (actionMasked == MotionEvent.ACTION_POINTER_UP && motionEvent.findPointerIndex(this.focusingPointerID) == (actionIndex = motionEvent.getActionIndex())) {
                    if (actionIndex == 0) {
                        z2 = true;
                    }
                    changeFocusingPointer(motionEvent, z2 ? 1 : 0);
                }
            }
        }
        return true;
    }
}
