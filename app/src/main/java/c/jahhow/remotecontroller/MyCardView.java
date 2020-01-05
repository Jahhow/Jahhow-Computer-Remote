package c.jahhow.remotecontroller;

import android.app.Activity;
import android.os.Build;
import androidx.cardview.widget.CardView;
import android.view.animation.Interpolator;
import android.widget.ImageView;

public class MyCardView extends CardView {
    public ImageView j;
    public float k;
    public boolean l = false;

    public MyCardView(Activity mVar, Interpolator interpolator) {
        super(mVar);
        SetupCardView(this);
        this.j = (ImageView) mVar.getLayoutInflater().inflate(R.layout.swipe_direction_indicator, this, false);
        this.j.animate().setInterpolator(interpolator);
        addView(this.j);
    }

    public static void SetupCardView(CardView cardView) {
        int i;
        float density = cardView.getResources().getDisplayMetrics().density;
        int i2 = 0;
        cardView.setPreventCornerOverlap(false);
        cardView.setRadius(36.0f * density);
        float f2 = 24.0f * density;
        cardView.setMaxCardElevation(f2);
        cardView.setCardElevation(f2);
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        if (Build.VERSION.SDK_INT >= 21) {
            i2 = (int) (density * 32.0f);
            i = i2;
        } else {
            i = cardView.getPaddingBottom() - cardView.getPaddingLeft();
        }
        layoutParams.setMargins(i, i2, i, i2);
        cardView.setLayoutParams(layoutParams);
    }

    public void a(float f) {
        if (!this.l) {
            this.l = true;
            this.j.animate().alpha(1.0f);
        }
        float f2 = (f - this.k) % 360.0f;
        if (f2 == 0.0f) {
            return;
        }
        if (this.j.getAlpha() == 0.0f) {
            this.j.setRotation(f);
            this.k = f;
            return;
        }
        if (f2 > 180.0f) {
            f2 -= 360.0f;
        } else if (f2 < -180.0f) {
            f2 += 360.0f;
        }
        this.k += f2;
        this.j.animate().rotation(this.k);
    }

    public void a(int i) {
        a(((float) i) * 90.0f);
    }

    public void a(boolean z) {
        if (this.l) {
            this.l = false;
            if (z) {
                this.j.animate().alpha(0.0f);
                return;
            }
            this.j.animate().cancel();
            this.j.setAlpha(0.0f);
        }
    }
}
