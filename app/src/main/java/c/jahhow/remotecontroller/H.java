package c.jahhow.remotecontroller;

import android.view.animation.Interpolator;

class H implements Runnable {
    private final float f1692a;
    private final Interpolator f1693b;
    private final SwipeCardDemoRunnable f1694c;

    H(SwipeCardDemoRunnable swipeCardDemoRunnable, float f, Interpolator interpolator) {
        this.f1694c = swipeCardDemoRunnable;
        this.f1692a = f;
        this.f1693b = interpolator;
    }

    public void run() {
        this.f1694c.airMouseLayout.mouseCardView.animate().translationY(this.f1692a).setDuration(1250).setInterpolator(this.f1693b);
    }
}
