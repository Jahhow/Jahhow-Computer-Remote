package c.jahhow.remotecontroller;

import android.view.animation.Interpolator;

class H implements Runnable {

    /* renamed from: a  reason: collision with root package name */
    public final /* synthetic */ float f1692a;

    /* renamed from: b  reason: collision with root package name */
    public final /* synthetic */ Interpolator f1693b;

    /* renamed from: c  reason: collision with root package name */
    public final /* synthetic */ J f1694c;

    public H(J j, float f, Interpolator interpolator) {
        this.f1694c = j;
        this.f1692a = f;
        this.f1693b = interpolator;
    }

    public void run() {
        this.f1694c.f1698a.mouseCardView.animate().translationY(this.f1692a).setDuration(1250).setInterpolator(this.f1693b);
    }
}
