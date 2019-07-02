package c.jahhow.remotecontroller;

import android.view.MotionEvent;
import android.view.View;

public abstract class LongPressAndUpDetector implements View.OnTouchListener, View.OnLongClickListener {

	abstract void onLongClickUp(View v);

	abstract void onLongClickDown(View v);

	LongPressAndUpDetector(View v) {
		v.setOnTouchListener(this);
		v.setOnLongClickListener(this);
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
				checkLongUp(v);
				break;
			case MotionEvent.ACTION_CANCEL:
				checkLongUp(v);
				break;
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
		onLongClickDown(v);
		return true;
	}
}
