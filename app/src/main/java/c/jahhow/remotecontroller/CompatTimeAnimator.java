package c.jahhow.remotecontroller;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;

class CompatTimeAnimator extends ValueAnimator {
    CompatTimeAnimator() {
        setDuration(Long.MAX_VALUE);
        setValues(PropertyValuesHolder.ofInt("", 0));
        setRepeatCount(INFINITE);
    }
}
