package c.jahhow.remotecontroller;

class K implements Runnable {

    /* renamed from: a  reason: collision with root package name */
    public final /* synthetic */ MotionMouseLayout f1699a;

    public K(MotionMouseLayout motionMouseLayout) {
        this.f1699a = motionMouseLayout;
    }

    public void run() {
        MotionMouseLayout motionMouseLayout = this.f1699a;
        if (motionMouseLayout.y && (!motionMouseLayout.w)) {
            motionMouseLayout.mouseCardView.Indicate(0);
        }
    }
}
