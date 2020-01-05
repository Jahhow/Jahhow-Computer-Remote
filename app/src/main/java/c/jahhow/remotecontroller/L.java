package c.jahhow.remotecontroller;

import c.jahhow.remotecontroller.MotionMouseCardView;

class L implements Runnable {

    /* renamed from: a  reason: collision with root package name */
    public final /* synthetic */ M f1700a;

    public L(M m) {
        this.f1700a = m;
    }

    public void run() {
        M m = this.f1700a;
        m.I--;
        MotionMouseCardView motionMouseCardView = m.f1703c;
        if (motionMouseCardView.l == motionMouseCardView.o && m.I == 0) {
            motionMouseCardView.a(-1);
        }
    }
}
