package c.jahhow.remotecontroller;

import android.view.animation.Interpolator;

class I implements Runnable {

    /* renamed from: a  reason: collision with root package name */
    public final /* synthetic */ float f1695a;

    /* renamed from: b  reason: collision with root package name */
    public final /* synthetic */ Interpolator f1696b;

    /* renamed from: c  reason: collision with root package name */
    public final /* synthetic */ J f1697c;

    public I(J j, float f, Interpolator interpolator) {
        this.f1697c = j;
        this.f1695a = f;
        this.f1696b = interpolator;
    }

    public void run() {
        this.f1697c.f1698a.mouseCardView.animate().translationX(this.f1695a).setDuration(1250).setInterpolator(this.f1696b);
    }
}