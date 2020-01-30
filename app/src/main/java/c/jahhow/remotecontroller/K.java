package c.jahhow.remotecontroller;

class K implements Runnable {

    private final AirMouseLayout airMouseLayout;

    K(AirMouseLayout airMouseLayout) {
        this.airMouseLayout = airMouseLayout;
    }

    public void run() {
        AirMouseLayout airMouseLayout = this.airMouseLayout;
        if (airMouseLayout.y && (!airMouseLayout.aFocusingPointerActuallyMoved)) {
            airMouseLayout.mouseCardView.Indicate(0);
        }
    }
}
