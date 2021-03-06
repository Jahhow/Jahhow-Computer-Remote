package c.jahhow.remotecontroller;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

public class AirMouseCardView extends CardView {
    private Interpolator j;
    private int k = -1;
    public View l = null;
    private View m;
    private View n;
    public View o;
    private View p;
    private View q;
    private final long r = 700;
    private final long s = 100;
    private boolean t = false;

    public AirMouseCardView(@NonNull Context context) {
        super(context);
    }

    public AirMouseCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AirMouseCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void Indicate(int i) {
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

    public void Init(Interpolator interpolator) {
        MyCardViewSetup.Setup(this);
        this.j = interpolator;
        grabViews();
    }

    private void grabViews() {
        this.m = findViewById(R.id.pause);
        this.n = findViewById(R.id.mouseRight);
        this.o = findViewById(R.id.mouseLeft);
        this.p = findViewById(R.id.scroll);
        this.q = findViewById(R.id.perpendicularWarn);
    }

    public void d() {
        if (this.t) {
            this.t = false;
            Indicate(this.k);
        }
    }
}
