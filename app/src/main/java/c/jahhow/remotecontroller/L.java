package c.jahhow.remotecontroller;

class L implements Runnable {

    /* renamed from: a  reason: collision with root package name */
    public final /* synthetic */ MotionMouseLayout f1700a;

    public L(MotionMouseLayout motionMouseLayout) {
        this.f1700a = motionMouseLayout;
    }

    public void run() {
        MotionMouseLayout motionMouseLayout = this.f1700a;
        motionMouseLayout.I--;
        MotionMouseCardView motionMouseCardView = motionMouseLayout.mouseCardView;
        if (motionMouseCardView.l == motionMouseCardView.o && motionMouseLayout.I == 0) {
            motionMouseCardView.Indicate(-1);
        }
    }
}
