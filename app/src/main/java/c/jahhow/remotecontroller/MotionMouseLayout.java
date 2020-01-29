package c.jahhow.remotecontroller;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

@SuppressLint("ViewConstructor")
public class MotionMouseLayout extends FrameLayout implements ValueAnimator.AnimatorUpdateListener {
    public double scrollBuffer;
    public static final float expSmoothTimeConstant = .2582437027204f;
    public float scale = 3.0f;
    public float animationDiffX = 0.0f;
    public float animationDiffY = 0.0f;
    public ExponentialSmoothing smootherY;
    public ExponentialSmoothing smootherX;
    public int I = 0;
    public Runnable J = new K(this);
    public Runnable K = new L(this);
    public boolean L;
    public float M;
    public MainActivity f1701a;
    public MotionMouseFragment f1702b;
    public MotionMouseCardView mouseCardView;
    public ValueAnimator f1704d = new CompatTimeAnimator();
    public Interpolator decelerateInterpolator = new DecelerateInterpolator(2.0f);
    public Interpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    public int focusingPointerID;
    public float focusingPointerOriginX;
    public float focusingPointerOriginY;
    public float diffX;
    public float diffY;
    public float startFocusingX;
    public float startFocusingY;
    public float n;
    public float focusingPointerLastY;
    public final float density = getResources().getDisplayMetrics().density;
    public float q = (this.density * 4.0f);
    public long r = 800;
    public boolean attachedToWindow;
    public int t = 300;
    public int u = 300;
    public boolean preventJump = false;
    public boolean aFocusingPointerActuallyMoved;
    public boolean scroll;
    public boolean y;
    public int maxPointerCount;

    public MotionMouseLayout(MainActivity mainActivity, MotionMouseFragment motionMouseFragment) {
        super(mainActivity);
        float refreshRate = mainActivity.getWindowManager().getDefaultDisplay().getRefreshRate();
        float refreshRateAware_NewValueWeight = (float) (1 - Math.exp(-1 / refreshRate / expSmoothTimeConstant));
        smootherY = new ExponentialSmoothing(refreshRateAware_NewValueWeight);
        smootherX = new ExponentialSmoothing(refreshRateAware_NewValueWeight);

        this.mouseCardView = (MotionMouseCardView) mainActivity.getLayoutInflater().inflate(R.layout.motion_mouse_card_view, this, false);
        this.mouseCardView.Init(this.accelerateDecelerateInterpolator);
        this.mouseCardView.animate();
        addView(this.mouseCardView);
        setKeepScreenOn(true);
        this.f1704d.addUpdateListener(this);
        this.f1701a = mainActivity;
        this.f1702b = motionMouseFragment;
    }

    public void SwitchOrigin(float x, float y, float newX, float newY) {
        focusingPointerOriginX += (newX - x);
        focusingPointerOriginY += (newY - y);
    }

    public void a(int i2) {
        this.mouseCardView.Indicate(i2);
        this.mouseCardView.animate().setInterpolator(this.decelerateInterpolator).setDuration(this.r).translationX(0.0f).translationY(0.0f);
    }

    public void changeFocusingPointer(MotionEvent motionEvent, int newPointerIndex) {
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

    public void b(MotionEvent motionEvent, int focusingPointerIndex) {
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

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return true;
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent motionEvent) {
        MotionMouseCardView motionMouseCardView;
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
            this.f1702b.a(1);
            postDelayed(this.J, (long) this.t);
        } else {
            int i2 = -1;
            int i3 = 3;
            if (actionMasked == MotionEvent.ACTION_UP) {
                if (this.aFocusingPointerActuallyMoved) {
                    if (!this.scroll) {
                        this.f1701a.Vibrate();
                        if (this.L) {
                            this.f1701a.SendMouseLeftUp();
                        } else {
                            this.f1701a.SendMouseRightUp();
                        }
                    }
                    this.f1704d.cancel();
                } else if (this.maxPointerCount == 1 && motionEvent.getEventTime() - motionEvent.getDownTime() < ((long) this.t)) {
                    this.f1701a.Vibrate();
                    this.f1701a.SendMouseLeftClick();
                    this.I++;
                    postDelayed(this.K, (long) this.u);
                    i2 = 3;
                }
                this.f1702b.a(0);
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
                                    f1701a.SendMouseWheel(round);
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
                                    this.f1701a.Vibrate();
                                    if (z2) {
                                        this.f1701a.SendMouseRightUp();
                                        this.f1701a.SendMouseLeftDown();
                                        motionMouseCardView = this.mouseCardView;
                                    } else {
                                        this.f1701a.SendMouseLeftUp();
                                        this.f1701a.SendMouseRightDown();
                                        motionMouseCardView = this.mouseCardView;
                                        i3 = 1;
                                    }
                                    motionMouseCardView.Indicate(i3);
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
                        this.f1701a.Vibrate();
                        this.scroll = absDiffY > absDiffX;
                        if (this.scroll) {
                            this.focusingPointerLastY = curFocusingY;
                            this.smootherY.setValue(mouseCardView.getTranslationY());
                            this.scrollBuffer = 0.0f;
                        } else {
                            this.n = curFocusingX;
                            this.smootherX.setValue(mouseCardView.getTranslationX());
                            this.f1702b.a(0);
                            if (this.diffX < 0.0f) {
                                z2 = true;
                            }
                            this.L = z2;
                            if (this.L) {
                                this.f1701a.SendMouseLeftDown();
                                i4 = 3;
                            } else {
                                this.f1701a.SendMouseRightDown();
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
