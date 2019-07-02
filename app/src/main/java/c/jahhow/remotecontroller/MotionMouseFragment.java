package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MotionMouseFragment extends Fragment implements SensorEventListener {
	MainActivity mainActivity;
	private SensorManager sensorManager;
	private Sensor sensor;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sensorManager = (SensorManager) mainActivity.getSystemService(Activity.SENSOR_SERVICE);
		if (sensorManager == null) {
			mainActivity.ShowToast("Problem Getting Sensor Manager");
		} else {
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.motion_mouse, container, false);
		AppCompatButton mouseLeftButton = layout.findViewById(R.id.MouseleftButton);
		AppCompatButton mouseRightButton = layout.findViewById(R.id.MouseRightButton);
		ImageView imagePauseMoving = layout.findViewById(R.id.ImagePauseMoving);

		mouseLeftButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:
						mainActivity.SendMouseLeftDown();
						return true;
					case MotionEvent.ACTION_UP:
						mainActivity.SendMouseLeftUp();
						return true;
				}
				return false;
			}
		});
		mouseRightButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:
						mainActivity.SendMouseRightDown();
						return true;
					case MotionEvent.ACTION_UP:
						mainActivity.SendMouseRightUp();
						return true;
				}
				return false;
			}
		});
		return layout;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!sensorManager.registerListener(this, sensor, 16667/* 1/60 sec*/)) {
			mainActivity.ShowToast("Problem Using Sensor");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//SensorManager.getRotationMatrixFromVector();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
