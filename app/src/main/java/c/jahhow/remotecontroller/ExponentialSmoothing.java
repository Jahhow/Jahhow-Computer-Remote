package c.jahhow.remotecontroller;

public class ExponentialSmoothing {

    /* renamed from: a  reason: collision with root package name */
    public float value;

    /* renamed from: b  reason: collision with root package name */
    public float newValueWeight;

    /* renamed from: c  reason: collision with root package name */
    public float oldValueWeight;

    public ExponentialSmoothing(float f) {
        this.newValueWeight = f;
        this.oldValueWeight = 1.0f - f;
    }
}
