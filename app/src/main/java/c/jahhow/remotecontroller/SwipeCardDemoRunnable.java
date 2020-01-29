package c.jahhow.remotecontroller;

public class SwipeCardDemoRunnable implements Runnable {

    final MotionMouseLayout motionMouseLayout;

    SwipeCardDemoRunnable(MotionMouseLayout motionMouseLayout) {
        this.motionMouseLayout = motionMouseLayout;
    }

    public void run() {
        //Log.i(MotionMouseLayout.class.getSimpleName(), "Start DEMO LOOP");
        float f = this.motionMouseLayout.density * -32.0f;
        O o = new O(5);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        while (true) {
            MotionMouseLayout motionMouseLayout = this.motionMouseLayout;
            if (!motionMouseLayout.attachedToWindow) {
                break;
            }
            motionMouseLayout.post(new H(this, f, o));
            try {
                Thread.sleep(1250);
            } catch (InterruptedException ignored) {
            }
            MotionMouseLayout motionMouseLayout2 = this.motionMouseLayout;
            if (!motionMouseLayout2.attachedToWindow) {
                break;
            }
            motionMouseLayout2.post(new I(this, f, o));
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ignored) {
            }
        }
        //Log.i(MotionMouseLayout.class.getSimpleName(), " End  DEMO LOOP");
    }
}
