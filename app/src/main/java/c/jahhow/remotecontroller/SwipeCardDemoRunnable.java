package c.jahhow.remotecontroller;

class SwipeCardDemoRunnable implements Runnable {

    private final AirMouseLayout airMouseLayout;

    SwipeCardDemoRunnable(AirMouseLayout airMouseLayout) {
        this.airMouseLayout = airMouseLayout;
    }

    public void run() {
        //Log.i(MotionMouseLayout.class.getSimpleName(), "Start DEMO LOOP");
        final float translation = airMouseLayout.density * -32.0f;
        final BounceInterpolator bounceInterpolator = new BounceInterpolator(5);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        while (true) {
            if (!airMouseLayout.attachedToWindow) {
                break;
            }
            airMouseLayout.post(new Runnable() {
                @Override
                public void run() {
                    airMouseLayout.mouseCardView.animate().translationY(translation).setDuration(1250).setInterpolator(bounceInterpolator);
                }
            });
            try {
                Thread.sleep(1250);
            } catch (InterruptedException ignored) {
            }
            AirMouseLayout airMouseLayout2 = this.airMouseLayout;
            if (!airMouseLayout2.attachedToWindow) {
                break;
            }
            airMouseLayout2.post(new Runnable() {
                @Override
                public void run() {
                    airMouseLayout.mouseCardView.animate().translationX(translation).setDuration(1250).setInterpolator(bounceInterpolator);
                }
            });
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ignored) {
            }
        }
        //Log.i(MotionMouseLayout.class.getSimpleName(), " End  DEMO LOOP");
    }
}
