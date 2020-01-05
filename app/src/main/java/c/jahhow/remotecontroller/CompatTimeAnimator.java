package c.jahhow.remotecontroller;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;

public class CompatTimeAnimator extends ValueAnimator {
    public CompatTimeAnimator() {
        setDuration(Long.MAX_VALUE);
        setValues(new PropertyValuesHolder[]{PropertyValuesHolder.ofInt("", new int[]{0})});
        setRepeatCount(-1);
    }
}
