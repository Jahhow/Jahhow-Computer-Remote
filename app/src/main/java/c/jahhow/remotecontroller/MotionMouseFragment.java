package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import static c.jahhow.remotecontroller.TouchPadView.GetAdjustFactor;

public class MotionMouseFragment extends Fragment implements SensorEventListener {
	MainActivity mainActivity;
	public MotionMouseCardView cardView;

	private SensorManager sensorManager;
	private Sensor rotationVectorSensor;
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
		M m = new M(mainActivity,this);
		this.cardView = m.f1703c;
		if (sensorManager == null) {
			sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
			rotationVectorSensor = sensorManager.getDefaultSensor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? Sensor.TYPE_GAME_ROTATION_VECTOR : Sensor.TYPE_ROTATION_VECTOR);
		}
		if (rotationVectorSensor == null) {
			Toast.makeText(getContext(), getContext().getString(R.string.This_device_doesnt_supportMotionMouse), Toast.LENGTH_LONG).show();
		}
		return m;
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

	float speed = 2048;
	float scrollSpeed = 1.5f * speed;
	float driftScrollSpeed = .015625f * speed;

	final float[]
			mainVector = {0, 1, 0},
			rotatedVector = new float[3];

	double originAcosZ;
	float originRotateZ;
	float diffDriftScrollZdp;
	boolean hasSetOrigin = false;
	boolean pauseMovingMouse = false;
	boolean scroll = false;
	boolean driftScroll = false;

	double moveMouseAdjExp = 1.2;
	float upperBoundZ = .9375f;
	float _1minusUpperBoundZ = 1 - upperBoundZ;
	float square_1minusUpperBoundZ = _1minusUpperBoundZ * _1minusUpperBoundZ;

	void PauseMovingMouse() {
		hasSetOrigin = false;
		pauseMovingMouse = true;
	}

	void ResumeMovingMouse() {
		pauseMovingMouse = false;
	}

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
		//Log.i("onSensorChanged", String.valueOf(++i));
		if (!pauseMovingMouse) {
			if (hasSetOrigin) {
				SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
				MatrixMultiply(rotationMatrix, mainVector, rotatedVector);
				float rotateZ = (float) Math.atan2(rotatedVector[0], rotatedVector[1]);
				float z = rotatedVector[2];

				double diffRotateZdp;
				float diffRotateZ = rotateZ - originRotateZ;
				if (diffRotateZ > Math.PI) {
					diffRotateZ -= 2 * Math.PI;
				} else if (diffRotateZ < -Math.PI) {
					diffRotateZ += 2 * Math.PI;
				}
				diffRotateZdp = diffRotateZ;// if float, can see significant drifting
				float absZ = Math.abs(z);
				if (absZ > upperBoundZ) {
					float r = 1 - absZ;
					diffRotateZdp = diffRotateZdp * r * r / (square_1minusUpperBoundZ);
					this.cardView.a(4);
				} else {
					this.cardView.d();
				}

				float diffArcCosZdp = (float) (Math.acos(z) - originAcosZ);

				float adjFactor = (float) GetAdjustFactor(diffRotateZdp, diffArcCosZdp, moveMouseAdjExp);
				adjFactor *= speed;
				int roundAdjDiffRotateZdp = (int) Math.round(adjFactor * diffRotateZdp);
				int roundAdjDiffArcCosZdp = Math.round(adjFactor * diffArcCosZdp);
				if (roundAdjDiffRotateZdp != 0 || roundAdjDiffArcCosZdp != 0) {
					mainActivity.SendMouseMove((short) roundAdjDiffRotateZdp, (short) roundAdjDiffArcCosZdp);
					originRotateZ += roundAdjDiffRotateZdp / adjFactor;
					originAcosZ += roundAdjDiffArcCosZdp / adjFactor;
				}
			} else {
				SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
				MatrixMultiply(rotationMatrix, mainVector, rotatedVector);
				originRotateZ = (float) Math.atan2(rotatedVector[0], rotatedVector[1]);
				originAcosZ = Math.acos(rotatedVector[2]);
				hasSetOrigin = true;
			}
		}
	}

	void MatrixMultiply(float[] leftM, float[] vector, float[] result) {
		result[0] = leftM[0] * vector[0] + leftM[1] * vector[1] + leftM[2] * vector[2];
		result[1] = leftM[3] * vector[0] + leftM[4] * vector[1] + leftM[5] * vector[2];
		result[2] = leftM[6] * vector[0] + leftM[7] * vector[1] + leftM[8] * vector[2];
	}

	@Override
	@Keep
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}