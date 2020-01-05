package c.jahhow.remotecontroller;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.animation.Interpolator;
import android.widget.ImageView;

public class SwipeCardView extends CardView {
	ImageView indicator;
	public static final int IndicatorUp = 0, IndicatorRight = 1, IndicatorDown = 2, IndicatorLeft = 3;

	public SwipeCardView(@NonNull AppCompatActivity activity, Interpolator interpolator) {
		super(activity);
		float density = getResources().getDisplayMetrics().density;
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		int marginVerticalDp = (int) ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 32 : 0) * density);
		int marginDpHorizontal = (int) ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 32 : 12) * density);
		layoutParams.setMargins(marginDpHorizontal, marginVerticalDp, marginDpHorizontal, marginVerticalDp);
		setLayoutParams(layoutParams);
		setRadius(36 * density);
		float elevationDp = 24 * density;
		setMaxCardElevation(elevationDp);
		setCardElevation(elevationDp);

		indicator = (ImageView) activity.getLayoutInflater().inflate(R.layout.swipe_direction_indicator, this, false);
		indicator.setAlpha(0f);
		indicator.animate().setInterpolator(interpolator);
		addView(indicator);
	}

	void Indicate(int newDirection) {
		Indicate(newDirection * 90f);
	}

	float indicatingDegree;
	boolean isShowingIndicator = false;

	void Indicate(float newDegree) {
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