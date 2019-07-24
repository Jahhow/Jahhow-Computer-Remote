package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import static c.jahhow.remotecontroller.TouchPadLayout.GetAdjustFactor;

public class MotionMouseFragment extends Fragment implements SensorEventListener {
	MainActivity mainActivity;
	View layout = null;

	private SensorManager sensorManager;
	private Sensor rotationVectorSensor;
	private final float[] rotationMatrix = new float[9];

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainActivity = (MainActivity) getActivity();
	}

	Button mouseLeft, mouseRight;

	@SuppressLint("ClickableViewAccessibility")
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if (sensorManager == null) {
			sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
			rotationVectorSensor = sensorManager.getDefaultSensor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? Sensor.TYPE_GAME_ROTATION_VECTOR : Sensor.TYPE_ROTATION_VECTOR);
		}
		if (rotationVectorSensor == null) {
			Toast.makeText(getContext(), "This device doesn't support Motion Mouse", Toast.LENGTH_LONG).show();
		}
		layout = inflater.inflate(R.layout.motion_mouse, container, false);
		mouseLeft = layout.findViewById(R.id.MouseLeftButton);
		mouseLeft.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mainActivity.SendMouseLeftDown();
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_OUTSIDE:
						mainActivity.SendMouseLeftUp();
						break;
				}
				return false;
			}
		});
		mouseRight = layout.findViewById(R.id.MouseRightButton);
		mouseRight.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mainActivity.SendMouseRightDown();
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_OUTSIDE:
						mainActivity.SendMouseRightUp();
						break;
				}
				return false;
			}
		});
		return layout;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (rotationVectorSensor != null)
			sensorManager.registerListener(this, rotationVectorSensor, 16667/*1/60 sec*/);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (rotationVectorSensor != null)
			sensorManager.unregisterListener(this);
		hasLastRotateZ = false;
	}

	float speed = 512;

	final float[]
			mainVector = {0, 1, 0},
			rotatedVector = new float[3];

	double originAcosZ;
	float originRotateZ;
	boolean hasLastRotateZ = false;

	double moveMouseAdjExp = 1.2;
	float upperBoundZ = .9f;
	float _1minusUpperBoundZ = 1 - upperBoundZ;

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (hasLastRotateZ) {
			SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
			MatrixMultiply(rotationMatrix, mainVector, rotatedVector);
			float rotateZ = (float) Math.atan2(rotatedVector[0], rotatedVector[1]);
			float diffRotateZ = rotateZ - originRotateZ;
			if (diffRotateZ > Math.PI) {
				diffRotateZ -= 2 * Math.PI;
			} else if (diffRotateZ < -Math.PI) {
				diffRotateZ += 2 * Math.PI;
			}
			float z = rotatedVector[2];
			float diffRotateZdp = diffRotateZ * speed;
			float absZ = Math.abs(z);
			if (absZ > upperBoundZ) {
				diffRotateZdp *= (1 - absZ) / (_1minusUpperBoundZ);
			}
			float diffArcCosZdp = (float) ((Math.acos(z) - originAcosZ) * speed /* * Math.PI / 2*/);// *PI/2 is for converting unit to be as adjustedDiffRotateZ
			float adjFactor = (float) GetAdjustFactor(diffRotateZdp, diffArcCosZdp, moveMouseAdjExp);
			int roundAdjDiffRotateZdp = Math.round(adjFactor * diffRotateZdp);
			int roundAdjDiffArcCosZdp = Math.round(adjFactor * diffArcCosZdp);
			if (roundAdjDiffRotateZdp != 0 || roundAdjDiffArcCosZdp != 0) {
				mainActivity.SendMouseMove((short) roundAdjDiffRotateZdp, (short) roundAdjDiffArcCosZdp);
				originRotateZ += roundAdjDiffRotateZdp / (adjFactor * speed);
				originAcosZ += roundAdjDiffArcCosZdp / (adjFactor * speed);
			}
		} else {
			SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
			MatrixMultiply(rotationMatrix, mainVector, rotatedVector);
			originRotateZ = (float) Math.atan2(rotatedVector[0], rotatedVector[1]);
			originAcosZ = Math.acos(rotatedVector[2]);
			hasLastRotateZ = true;
		}
	}

	void MatrixMultiply(float[] leftM, float[] vector, float[] result) {
		result[0] = leftM[0] * vector[0] + leftM[1] * vector[1] + leftM[2] * vector[2];
		result[1] = leftM[3] * vector[0] + leftM[4] * vector[1] + leftM[5] * vector[2];
		result[2] = leftM[6] * vector[0] + leftM[7] * vector[1] + leftM[8] * vector[2];
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}