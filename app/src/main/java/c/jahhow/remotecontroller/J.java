package c.jahhow.remotecontroller;

import android.util.Log;

public class J implements Runnable {

    /* renamed from: a  reason: collision with root package name */
    public final /* synthetic */ M f1698a;

    public J(M m) {
        this.f1698a = m;
    }

    public void run() {
        Log.i(M.class.getSimpleName(), "Start DEMO LOOP");
        float f = this.f1698a.p * -32.0f;
        O o = new O(5);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException unused) {
        }
        while (true) {
            M m = this.f1698a;
            if (!m.s) {
                break;
            }
            m.post(new H(this, f, o));
            try {
                Thread.sleep(1250);
            } catch (InterruptedException unused2) {
            }
            M m2 = this.f1698a;
            if (!m2.s) {
                break;
            }
            m2.post(new I(this, f, o));
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ignored) {
            }
        }
        Log.i(M.class.getSimpleName(), " End  DEMO LOOP");
    }
}
