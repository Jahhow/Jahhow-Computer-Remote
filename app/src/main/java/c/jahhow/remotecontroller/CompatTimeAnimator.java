package c.jahhow.remotecontroller;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;

public class CompatTimeAnimator extends ValueAnimator {
    public CompatTimeAnimator() {
        setDuration(Long.MAX_VALUE);
        setValues(PropertyValuesHolder.ofInt("", 0));
        setRepeatCount(INFINITE);
    }
}
