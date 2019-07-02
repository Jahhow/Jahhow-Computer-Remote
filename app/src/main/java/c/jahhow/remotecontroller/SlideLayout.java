package c.jahhow.remotecontroller;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class SlideLayout extends FrameLayout {
	ImageView indicator;
	public static final int IndicatorUp = 0, IndicatorRight = 1, IndicatorDown = 2, IndicatorLeft = 3;
	MainActivity mainActivity;

	public SlideLayout(@NonNull Activity activity, Interpolator interpolator) {
		super(activity);
		mainActivity = (MainActivity) activity;
		float density = getResources().getDisplayMetrics().density;
		int marginDp = (int) (32 * density);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.setMargins(marginDp, marginDp, marginDp, marginDp);
		setLayoutParams(layoutParams);
		setBackgroundResource(R.drawable.slide);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setElevation(24 * density);
		}

		indicator = (ImageView) activity.getLayoutInflater().inflate(R.layout.swipe_direction_indicator, this, false);
		indicator.setAlpha(0f);
		indicator.animate().setInterpolator(interpolator);
		indicator.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
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
