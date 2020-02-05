package c.jahhow.remotecontroller;

class SwipeCardDemoRunnable implements Runnable {

    final AirMouseLayout airMouseLayout;

    SwipeCardDemoRunnable(AirMouseLayout airMouseLayout) {
        this.airMouseLayout = airMouseLayout;
    }

    public void run() {
        //Log.i(MotionMouseLayout.class.getSimpleName(), "Start DEMO LOOP");
        float f = this.airMouseLayout.density * -32.0f;
        O o = new O(5);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        while (true) {
            AirMouseLayout airMouseLayout = this.airMouseLayout;
            if (!airMouseLayout.attachedToWindow) {
                break;
            }
            airMouseLayout.post(new H(this, f, o));
            try {
                Thread.sleep(1250);
            } catch (InterruptedException ignored) {
            }
            AirMouseLayout airMouseLayout2 = this.airMouseLayout;
            if (!airMouseLayout2.attachedToWindow) {
                break;
            }
            airMouseLayout2.post(new I(this, f, o));
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ignored) {
            }
        }
        //Log.i(MotionMouseLayout.class.getSimpleName(), " End  DEMO LOOP");
    }
}
