package c.jahhow.remotecontroller;

import android.os.Build;
import android.view.ViewGroup.MarginLayoutParams;

import androidx.cardview.widget.CardView;

class MyCardViewSetup {
    static void Setup(CardView cardView) {
        int i;
        float density = cardView.getResources().getDisplayMetrics().density;
        int i2 = 0;
        cardView.setPreventCornerOverlap(false);
        cardView.setRadius(36.0f * density);
        float f2 = 24.0f * density;
        cardView.setMaxCardElevation(f2);
        cardView.setCardElevation(f2);
        MarginLayoutParams layoutParams = new MarginLayoutParams(-1, -1);
        if (Build.VERSION.SDK_INT >= 21) {
            i2 = (int) (density * 32.0f);
            i = i2;
        } else {
            i = cardView.getPaddingBottom() - cardView.getPaddingLeft();
        }
        layoutParams.setMargins(i, i2, i, i2);
        cardView.setLayoutParams(layoutParams);
    }
}