package c.jahhow.remotecontroller;

import android.util.Log;

public class J implements Runnable {

    /* renamed from: a  reason: collision with root package name */
    public final /* synthetic */ MotionMouseLayout f1698a;

    public J(MotionMouseLayout motionMouseLayout) {
        this.f1698a = motionMouseLayout;
    }

    public void run() {
        Log.i(MotionMouseLayout.class.getSimpleName(), "Start DEMO LOOP");
        float f = this.f1698a.density * -32.0f;
        O o = new O(5);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException unused) {
        }
        while (true) {
            MotionMouseLayout motionMouseLayout = this.f1698a;
            if (!motionMouseLayout.s) {
                break;
            }
            motionMouseLayout.post(new H(this, f, o));
            try {
                Thread.sleep(1250);
            } catch (InterruptedException unused2) {
            }
            MotionMouseLayout motionMouseLayout2 = this.f1698a;
            if (!motionMouseLayout2.s) {
                break;
            }
            motionMouseLayout2.post(new I(this, f, o));
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ignored) {
            }
        }
        Log.i(MotionMouseLayout.class.getSimpleName(), " End  DEMO LOOP");
    }
}
