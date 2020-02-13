package c.jahhow.remotecontroller;

import android.view.animation.Interpolator;

class BounceInterpolator implements Interpolator {

    /* renamed from: b  reason: collision with root package name */
    private static final double f1710b = Math.exp(-6.0d);

    /* renamed from: c  reason: collision with root package name */
    private final int f1711c;

    BounceInterpolator(int i) {
        this.f1711c = i;
    }

    public float getInterpolation(float f) {
        if (f == 1)
            return 0;
        double d2 = (double) (((float) this.f1711c) * f * 2.0f);
        /* renamed from: a  reason: collision with root package name */
        float f1709a = -6.0f;
        double exp = Math.exp((double) (f1709a * f)) * Math.sin(d2 * Math.PI);
        double d3 = (double) f;
        return (float) (exp - (d3 * f1710b));
    }
}
