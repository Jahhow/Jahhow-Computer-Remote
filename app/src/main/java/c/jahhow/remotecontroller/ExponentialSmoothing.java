package c.jahhow.remotecontroller;

class ExponentialSmoothing {
    private float value = 0;
    private float newValueWeight;
    private float oldValueWeight;

    ExponentialSmoothing(float newValueWeight) {
        this.newValueWeight = newValueWeight;
        this.oldValueWeight = 1.0f - newValueWeight;
    }

    float Smoothen(float value) {
        this.value = (this.value * oldValueWeight) + (value * newValueWeight);
        return this.value;
    }

    void setValue(float value) {
        this.value = value;
    }
}
