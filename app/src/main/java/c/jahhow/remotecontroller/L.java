package c.jahhow.remotecontroller;

class L implements Runnable {
    private final AirMouseLayout airMouseLayout;

    L(AirMouseLayout airMouseLayout) {
        this.airMouseLayout = airMouseLayout;
    }

    public void run() {
        AirMouseLayout airMouseLayout = this.airMouseLayout;
        airMouseLayout.I--;
        AirMouseCardView airMouseCardView = airMouseLayout.mouseCardView;
        if (airMouseCardView.l == airMouseCardView.o && airMouseLayout.I == 0) {
            airMouseCardView.Indicate(-1);
        }
    }
}
