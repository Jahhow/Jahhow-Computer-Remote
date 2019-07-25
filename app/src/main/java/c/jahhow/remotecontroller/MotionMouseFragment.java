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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import static c.jahhow.remotecontroller.TouchPadView.GetAdjustFactor;

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

	@SuppressLint("ClickableViewAccessibility")
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		layout = inflater.inflate(R.layout.motion_mouse, container, false);

		if (sensorManager == null) {
			sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
			rotationVectorSensor = sensorManager.getDefaultSensor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 ? Sensor.TYPE_GAME_ROTATION_VECTOR : Sensor.TYPE_ROTATION_VECTOR);
		}
		if (rotationVectorSensor == null) {
			Toast.makeText(getContext(), getContext().getString(R.string.This_device_doesnt_supportMotionMouse), Toast.LENGTH_LONG).show();
		} else {
			new TouchDownUpDetector(layout.findViewById(R.id.MouseLeftButton)) {
				@Override
				void OnDown() {
					mainActivity.SendMouseLeftDown();
				}

				@Override
				void OnUp() {
					mainActivity.SendMouseLeftUp();
				}
			};
			new TouchDownUpDetector(layout.findViewById(R.id.MouseRightButton)) {
				@Override
				void OnDown() {
					mainActivity.SendMouseRightDown();
				}

				@Override
				void OnUp() {
					mainActivity.SendMouseRightUp();
				}
			};
			new TouchDownUpDetector(layout.findViewById(R.id.ImagePauseMoving)) {
				@Override
				void OnDown() {
					PauseMovingMouse();
				}

				@Override
				void OnUp() {
					ResumeMovingMouse();
				}
			};
			new TouchDownUpDetector(layout.findViewById(R.id.imageScroll)) {
				@Override
				void OnDown() {
					scroll = true;
					hasSetOrigin = false;
				}

				@Override
				void OnUp() {
					scroll = false;
					hasSetOrigin = false;
				}
			};
			new TouchDownUpDetector(layout.findViewById(R.id.imageDriftScroll)) {
				@Override
				void OnDown() {
					driftScroll = true;
					diffDriftScrollZdp = 0;
					hasSetOrigin = false;
				}

				@Override
				void OnUp() {
					driftScroll = false;
					hasSetOrigin = false;
				}
			};
		}
		return layout;
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
				if (scroll | driftScroll) {
					diffRotateZdp = 0;
				} else {
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
					}
				}
				float diffArcCosZdp = (float) (Math.acos(z) - originAcosZ);

				if (driftScroll) {
					diffDriftScrollZdp += diffArcCosZdp;
					float d = diffDriftScrollZdp * driftScrollSpeed;
					int roundDiffDriftScroll = Math.round(d);
					if (roundDiffDriftScroll != 0) {
						mainActivity.SendMouseWheel(roundDiffDriftScroll);
						diffDriftScrollZdp -= roundDiffDriftScroll / driftScrollSpeed;
					}
				} else {
					float adjFactor = (float) GetAdjustFactor(diffRotateZdp, diffArcCosZdp, moveMouseAdjExp);
					if (scroll) {
						adjFactor *= scrollSpeed;
					} else {
						adjFactor *= speed;
					}
					int roundAdjDiffRotateZdp = (int) Math.round(adjFactor * diffRotateZdp);
					int roundAdjDiffArcCosZdp = Math.round(adjFactor * diffArcCosZdp);
					if (roundAdjDiffRotateZdp != 0 || roundAdjDiffArcCosZdp != 0) {
						if (scroll) {
							mainActivity.SendMouseWheel(roundAdjDiffArcCosZdp);
						} else {
							mainActivity.SendMouseMove((short) roundAdjDiffRotateZdp, (short) roundAdjDiffArcCosZdp);
						}
						originRotateZ += roundAdjDiffRotateZdp / adjFactor;
						originAcosZ += roundAdjDiffArcCosZdp / adjFactor;
					}
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
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}