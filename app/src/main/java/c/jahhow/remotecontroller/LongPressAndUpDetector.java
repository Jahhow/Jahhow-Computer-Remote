package c.jahhow.remotecontroller;

import android.view.MotionEvent;
import android.view.View;

abstract class LongPressAndUpDetector implements View.OnTouchListener, View.OnLongClickListener {

	abstract void onLongClickDown(View v);

	abstract void onLongClickUp(View v);

	final MainActivity mainActivity;
	private final long vibrateMs;

	LongPressAndUpDetector(View v, MainActivity mainActivity) {
		this(v, 30, mainActivity);
	}

	private LongPressAndUpDetector(View v, long vibrateMs, MainActivity mainActivity) {
		v.setOnTouchListener(this);
		v.setOnLongClickListener(this);
		this.mainActivity = mainActivity;
		this.vibrateMs = vibrateMs;
	}

	private boolean longPressed;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getActionMasked();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				longPressed = false;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_OUTSIDE:
				checkLongUp(v);
				break;
		}
		return false;
	}

	private void checkLongUp(View v) {
		if (longPressed) {
			longPressed = false;
			onLongClickUp(v);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		longPressed = true;
		mainActivity.Vibrate(vibrateMs);
		onLongClickDown(v);
		return true;
	}
}