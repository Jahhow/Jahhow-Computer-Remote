package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class AirMouseFragment extends Fragment implements SensorEventListener {
    private MainActivity mainActivity;
    private AirMouseCardView cardView;

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor = null;
    private final float[] rotationMatrix = new float[9];

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AirMouseLayout airMouseLayout = new AirMouseLayout(mainActivity, this);
        this.cardView = airMouseLayout.mouseCardView;
        if (sensorManager == null) {
            sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                rotationVectorSensor = sensorManager.getDefaultSensor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? Sensor.TYPE_GAME_ROTATION_VECTOR : Sensor.TYPE_ROTATION_VECTOR);
            }
        }
        if (rotationVectorSensor == null) {
            Toast.makeText(getContext(), getString(R.string.This_device_does_not_supportAirMouse), Toast.LENGTH_LONG).show();
        }
        return airMouseLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rotationVectorSensor != null)
            sensorManager.registerListener(this, rotationVectorSensor, /*20000*/16667/*1/60 sec*/);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (rotationVectorSensor != null)
            sensorManager.unregisterListener(this);
        hasSetOrigin = false;
    }

    private final float[]
            mainVector = {0, 1, 0},
            rotatedVector = new float[3];

    private double originAcosZ;
    private double originRotateZ;
    private boolean hasSetOrigin = false;
    private boolean pauseMovingMouse = false;

    private final float upperBoundZ = .9375f;
    private final float square_1minusUpperBoundZ = (1 - upperBoundZ) * (1 - upperBoundZ);

    public void a(int pauseMovingMouse) {
        boolean _c2 = pauseMovingMouse != 0;
        if (this.pauseMovingMouse != _c2) {
            this.pauseMovingMouse = _c2;
            this.hasSetOrigin = false;
        }
    }

    //int i = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!pauseMovingMouse) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            MatrixMultiply(rotationMatrix, mainVector, rotatedVector);
            float z = rotatedVector[2];
            double rotateZ = Math.atan2(rotatedVector[0], rotatedVector[1]);
            double acosZ = Math.acos(z);

            if (hasSetOrigin) {
                double diffRotateZ = NormalizeRadian(rotateZ - originRotateZ);
                float absZ = Math.abs(z);
                if (absZ > upperBoundZ) {
                    float r = 1 - absZ;
                    diffRotateZ = diffRotateZ * r * r / (square_1minusUpperBoundZ);
                    this.cardView.Indicate(4);
                } else {
                    this.cardView.d();
                }

                double diffZ = acosZ - originAcosZ;

                double adjFactor = MotionAdjuster.GetMultiplierV3(diffRotateZ, diffZ, 40, 500);
                int roundAdjDiffRotateZdp = (int) Math.round(adjFactor * diffRotateZ);
                int roundAdjDiffArcCosZdp = (int) Math.round(adjFactor * diffZ);
                if (roundAdjDiffRotateZdp != 0 || roundAdjDiffArcCosZdp != 0) {
                    mainActivity.SendMouseMove((short) roundAdjDiffRotateZdp, (short) roundAdjDiffArcCosZdp);
                    originRotateZ = rotateZ;
                    originAcosZ = acosZ;
                }
            } else {
                originRotateZ = rotateZ;
                originAcosZ = acosZ;
                hasSetOrigin = true;
            }
        }
    }

    // Keep radian in [-PI,PI]
    private double NormalizeRadian(double radian) {
        if (radian > Math.PI) {
            radian -= 2 * Math.PI;
        } else if (radian < -Math.PI) {
            radian += 2 * Math.PI;
        }
        return radian;
    }

    private void MatrixMultiply(float[] leftM, float[] vector, float[] result) {
        result[0] = leftM[0] * vector[0] + leftM[1] * vector[1] + leftM[2] * vector[2];
        result[1] = leftM[3] * vector[0] + leftM[4] * vector[1] + leftM[5] * vector[2];
        result[2] = leftM[6] * vector[0] + leftM[7] * vector[1] + leftM[8] * vector[2];
    }

    @Override
    @Keep
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}