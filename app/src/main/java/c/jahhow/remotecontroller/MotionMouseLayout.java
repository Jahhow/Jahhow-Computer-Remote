package c.jahhow.remotecontroller;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

@SuppressLint("ViewConstructor")
public class MotionMouseLayout extends FrameLayout implements ValueAnimator.AnimatorUpdateListener {
    public float A;
    public float B = 0.0625f;
    public float C = 3.0f;
    public float D = 0.0f;
    public float E = 0.0f;
    public N F = new N(this.B);
    public N G = new N(this.B);
    public double H = 1.2d;
    public int I = 0;
    public Runnable J = new K(this);
    public Runnable K = new L(this);
    public boolean L;
    public float M;

    /* renamed from: a  reason: collision with root package name */
    public MainActivity f1701a;

    /* renamed from: b  reason: collision with root package name */
    public MotionMouseFragment f1702b;

    /* renamed from: c  reason: collision with root package name */
    public MotionMouseCardView mouseCardView;

    /* renamed from: d  reason: collision with root package name */
    public ValueAnimator f1704d = new CompatTimeAnimator();

    /* renamed from: e  reason: collision with root package name */
    public Interpolator decelerateInterpolator = new DecelerateInterpolator(2.0f);
    public Interpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    public int g;
    public float h;
    public float i;
    public float j;
    public float k;
    public float l;
    public float m;
    public float n;
    public float o;
    public final float p = getResources().getDisplayMetrics().density;
    public float q = (this.p * 4.0f);
    public long r = 800;
    public boolean s;
    public int t = 300;
    public int u = 300;
    public boolean v = false;
    public boolean w;
    public boolean x;
    public boolean y;
    public int z;

    public MotionMouseLayout(MainActivity mainActivity, MotionMouseFragment motionMouseFragment) {
        super(mainActivity);
        this.mouseCardView = (MotionMouseCardView) mainActivity.getLayoutInflater().inflate(R.layout.motion_mouse_card_view, this, false);
        this.mouseCardView.Init(this.accelerateDecelerateInterpolator);
        this.mouseCardView.animate();
        addView(this.mouseCardView);
        setKeepScreenOn(true);
        this.f1704d.addUpdateListener(this);
        this.f1701a = mainActivity;
        this.f1702b = motionMouseFragment;
    }

    public void a(float f2, float f3, float f4, float f5) {
        this.h = (f4 - f2) + this.h;
        this.i = (f5 - f3) + this.i;
    }

    public void a(int i2) {
        this.mouseCardView.Indicate(i2);
        this.mouseCardView.animate().setInterpolator(this.decelerateInterpolator).setDuration(this.r).translationX(0.0f).translationY(0.0f);
    }

    public void a(MotionEvent motionEvent, int i2) {
        this.v = true;
        this.l = motionEvent.getX(i2);
        this.m = motionEvent.getY(i2);
        int findPointerIndex = motionEvent.findPointerIndex(this.g);
        if (this.x) {
            this.o = motionEvent.getY(i2);
        } else {
            a(motionEvent.getX(findPointerIndex), motionEvent.getY(findPointerIndex), motionEvent.getX(i2), motionEvent.getY(i2));
        }
        this.g = motionEvent.getPointerId(i2);
    }

    public void b(MotionEvent motionEvent, int i2) {
        this.j = motionEvent.getX(i2) - this.h;
        this.k = motionEvent.getY(i2) - this.i;
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (this.x) {
            MotionMouseCardView motionMouseCardView = this.mouseCardView;
            N n2 = this.F;
            n2.f1706a = (n2.f1706a * n2.f1708c) + (this.E * n2.f1707b);
            motionMouseCardView.setTranslationY(n2.f1706a * this.C);
            this.E = 0.0f;
            return;
        }
        MotionMouseCardView motionMouseCardView2 = this.mouseCardView;
        N n3 = this.G;
        n3.f1706a = (n3.f1706a * n3.f1708c) + (this.D * n3.f1707b);
        motionMouseCardView2.setTranslationX(n3.f1706a * this.C);
        this.D = 0.0f;
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.s = true;
        new Thread(new J(this)).start();
    }

    public void onDetachedFromWindow() {
        this.s = false;
        Log.i(MotionMouseLayout.class.getSimpleName(), "onDetachedFromWindow()");
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
        if (actionMasked != MotionEvent.ACTION_DOWN) {
            int i2 = -1;
            int i3 = 3;
            if (actionMasked != MotionEvent.ACTION_UP) {
                int i4 = 2;
                if (actionMasked == MotionEvent.ACTION_MOVE) {
                    int findPointerIndex = motionEvent.findPointerIndex(this.g);
                    float x2 = motionEvent.getX(findPointerIndex);
                    float y2 = motionEvent.getY(findPointerIndex);
                    if (this.w) {
                        if (this.x) {
                            if (this.v) {
                                this.E = 0.0f;
                                if (y2 != this.m) {
                                    this.o = y2;
                                    this.v = false;
                                }
                            } else {
                                this.E = y2 - this.o;
                                double d2 = (double) (this.E / this.p);
                                double a2 = TouchPadView.scrollAdjMultiplier * TouchPadView.GetAdjustFactor(0.0d, d2, this.H) * d2;
                                double d3 = (double) this.A;
                                this.A = (float) (d3 + a2);
                                int round = Math.round(this.A);
                                if (round != 0) {
                                    this.f1701a.SendMouseWheel(round);
                                    this.A -= (float) round;
                                }
                                this.o = y2;
                            }
                        } else if (this.v) {
                            this.D = 0.0f;
                            float f2 = this.l;
                            if (x2 != f2) {
                                this.n = x2;
                                this.v = false;
                                a(f2, this.m, x2, y2);
                            }
                        } else {
                            this.D = x2 - this.n;
                            this.n = x2;
                            b(motionEvent, findPointerIndex);
                            if (this.j < this.M) {
                                z2 = true;
                            }
                            if (this.L ^ z2) {
                                this.f1701a.s();
                                if (z2) {
                                    this.f1701a.r();
                                    this.f1701a.SendMouseLeftDown();
                                    motionMouseCardView = this.mouseCardView;
                                } else {
                                    this.f1701a.o();
                                    this.f1701a.q();
                                    motionMouseCardView = this.mouseCardView;
                                    i3 = 1;
                                }
                                motionMouseCardView.Indicate(i3);
                                this.M = -this.M;
                                this.L = z2;
                            }
                        }
                    } else if (!(x2 == this.l && y2 == this.m)) {
                        this.v = false;
                        this.w = true;
                        b(motionEvent, findPointerIndex);
                        float abs = Math.abs(this.j);
                        float abs2 = Math.abs(this.k);
                        this.f1701a.s();
                        this.x = abs2 > abs;
                        if (this.x) {
                            this.o = y2;
                            this.F.f1706a = this.mouseCardView.getTranslationY() / this.C;
                            this.A = 0.0f;
                        } else {
                            this.n = x2;
                            this.G.f1706a = this.mouseCardView.getTranslationX() / this.C;
                            this.f1702b.a(0);
                            if (this.j < 0.0f) {
                                z2 = true;
                            }
                            this.L = z2;
                            if (this.L) {
                                this.f1701a.SendMouseLeftDown();
                                i4 = 3;
                            } else {
                                this.f1701a.q();
                                i4 = 1;
                            }
                            a(this.l, this.m, x2, y2);
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
                } else if (actionMasked == 5) {
                    a(motionEvent, motionEvent.getActionIndex());
                    int pointerCount = motionEvent.getPointerCount();
                    if (pointerCount > this.z) {
                        this.z = pointerCount;
                    }
                } else if (actionMasked == 6 && motionEvent.findPointerIndex(this.g) == (actionIndex = motionEvent.getActionIndex())) {
                    if (actionIndex == 0) {
                        z2 = true;
                    }
                    a(motionEvent, z2 ? 1 : 0);
                }
            } else {
                if (this.w) {
                    if (!this.x) {
                        this.f1701a.s();
                        if (this.L) {
                            this.f1701a.o();
                        } else {
                            this.f1701a.r();
                        }
                    }
                    this.f1704d.cancel();
                } else if (this.z == 1 && motionEvent.getEventTime() - motionEvent.getDownTime() < ((long) this.t)) {
                    this.f1701a.s();
                    this.f1701a.m();
                    this.I++;
                    postDelayed(this.K, (long) this.u);
                    i2 = 3;
                }
                this.f1702b.a(0);
                a(i2);
                this.y = false;
            }
        } else {
            this.s = false;
            this.w = false;
            this.y = true;
            this.k = 0.0f;
            this.j = 0.0f;
            this.h = motionEvent.getX();
            this.i = motionEvent.getY();
            this.mouseCardView.getTranslationX();
            this.mouseCardView.getTranslationY();
            this.g = motionEvent.getPointerId(0);
            this.v = true;
            this.l = motionEvent.getX(0);
            this.m = motionEvent.getY(0);
            this.mouseCardView.animate().cancel();
            this.z = 1;
            this.f1702b.a(1);
            postDelayed(this.J, (long) this.t);
        }
        return true;
    }
}
