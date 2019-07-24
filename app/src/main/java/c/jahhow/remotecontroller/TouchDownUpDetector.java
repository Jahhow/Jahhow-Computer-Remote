package c.jahhow.remotecontroller;

import android.view.MotionEvent;
import android.view.View;

abstract class TouchDownUpDetector implements View.OnTouchListener {

	abstract void OnDown();

	abstract void OnUp();

	TouchDownUpDetector(View view) {
		view.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				OnDown();
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_OUTSIDE:
				OnUp();
				break;
		}
		return false;
	}
}
