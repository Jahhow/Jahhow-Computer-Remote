package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

@SuppressLint("ViewConstructor")
public class SwipeCardView extends CardView {
    private final ImageView indicator;
    public static final int IndicatorUp = 0, IndicatorRight = 1, IndicatorDown = 2, IndicatorLeft = 3;

    public SwipeCardView(@NonNull AppCompatActivity activity, Interpolator interpolator) {
        super(activity);
        MyCardViewSetup.Setup(this);

        indicator = (ImageView) activity.getLayoutInflater().inflate(R.layout.swipe_direction_indicator, this, false);
        indicator.setAlpha(0f);
        indicator.animate().setInterpolator(interpolator);
        addView(indicator);
    }

    void Indicate(int newDirection) {
        Indicate(newDirection * 90f);
    }

    private float indicatingDegree;
    private boolean isShowingIndicator = false;

    private void Indicate(float newDegree) {
        if (!isShowingIndicator) {
            isShowingIndicator = true;
            indicator.animate().alpha(1);
        }
        float diffDegree = (newDegree - indicatingDegree) % 360;
        if (diffDegree != 0) {
            if (indicator.getAlpha() == 0) {
                indicator.setRotation(newDegree);
                indicatingDegree = newDegree;
            } else {
                if (diffDegree > 180)
                    diffDegree -= 360;
                else if (diffDegree < -180)
                    diffDegree += 360;
                indicatingDegree += diffDegree;
                indicator.animate().rotation(indicatingDegree);
            }
        }
    }

    void Reset(boolean smoothly) {
        if (isShowingIndicator) {
            isShowingIndicator = false;
            if (smoothly)
                indicator.animate().alpha(0);
            else {
                indicator.animate().cancel();
                indicator.setAlpha(0f);
            }
        }
    }
}