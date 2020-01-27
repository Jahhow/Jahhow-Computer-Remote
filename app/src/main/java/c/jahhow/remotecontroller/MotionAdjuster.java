package c.jahhow.remotecontroller;

class MotionAdjuster {
    static double GetMultiplierV1(double dxDp, double dyDp, double base, double scale) {
        return scale * Math.pow(base, Math.sqrt(dxDp * dxDp + dyDp * dyDp));
    }

    static double GetMultiplierV2(double dxDp, double dyDp, double expFactor) {
        return Math.pow(dxDp * dxDp + dyDp * dyDp, .5 * (expFactor - 1));
    }

    static double GetMultiplierV3(double dxDp, double dyDp, double intensity, double scale) {
        double x = Math.sqrt(dxDp * dxDp + dyDp * dyDp);
        return scale * (intensity * x + 1);
    }

    static double GetDefaultMouseMoveMultiplier(double dxDp, double dyDp) {
        return GetMultiplierV3(dxDp, dyDp, .1, 1.2);
    }

    static double GetDefaultScrollMultiplier(double dyDp) {
        //return GetMultiplierV3(dxDp, dyDp, .4, 1);
        return .4 * dyDp + 1;
    }
}
