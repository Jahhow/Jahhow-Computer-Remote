package c.jahhow.remotecontroller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

public class MotionMouseCardView extends CardView {
    public Interpolator j;
    public int k = -1;
    public View l = null;
    public View m;
    public View n;
    public View o;
    public View p;
    public View q;
    public long r = 700;
    public long s = 100;
    public boolean t = false;

    public MotionMouseCardView(@NonNull Context context) {
        super(context);
    }

    public MotionMouseCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MotionMouseCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void a(int i) {
        View view;
        if (!this.t) {
            if (i == 0) {
                view = this.m;
            } else if (i == 1) {
                view = this.n;
            } else if (i == 2) {
                view = this.p;
            } else if (i == 3) {
                view = this.o;
            } else if (i != 4) {
                view = null;
            } else {
                this.t = true;
                view = this.q;
            }
            View view2 = this.l;
            if (view2 != view) {
                if (view2 != null) {
                    view2.animate().setDuration(this.r).alpha(0.0f);
                }
                if (view != null) {
                    view.animate().setDuration(this.s).setInterpolator(this.j).alpha(1.0f);
                }
                this.l = view;
            }
        }
        if (i != 4) {
            this.k = i;
        }
    }

    public void a(Interpolator interpolator) {
        V.a((CardView) this);
        this.j = interpolator;
        c();
    }

    public void c() {
        this.m = findViewById(R.id.pause);
        this.n = findViewById(R.id.mouseRight);
        this.o = findViewById(R.id.mouseLeft);
        this.p = findViewById(R.id.scroll);
        this.q = findViewById(R.id.perpendicularWarn);
    }

    public void d() {
        if (this.t) {
            this.t = false;
            a(this.k);
        }
    }
}
