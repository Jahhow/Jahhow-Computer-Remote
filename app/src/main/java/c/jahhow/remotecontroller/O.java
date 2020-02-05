package c.jahhow.remotecontroller;

import android.view.animation.Interpolator;

class O implements Interpolator {

    /* renamed from: b  reason: collision with root package name */
    private static final double f1710b = Math.exp(-6.0d);

    /* renamed from: c  reason: collision with root package name */
    private final int f1711c;

    O(int i) {
        this.f1711c = i;
    }

    public float getInterpolation(float f) {
        double d2 = (double) (((float) this.f1711c) * f * 2.0f);
        /* renamed from: a  reason: collision with root package name */
        float f1709a = -6.0f;
        double exp = Math.exp((double) (f1709a * f)) * Math.sin(d2 * 3.141592653589793d);
        double d3 = (double) f;
        return (float) (exp - (d3 * f1710b));
    }
}
