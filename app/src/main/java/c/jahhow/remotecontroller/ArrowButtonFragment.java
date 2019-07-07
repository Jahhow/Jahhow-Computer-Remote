package c.jahhow.remotecontroller;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import c.jahhow.remotecontroller.Msg.ButtonAction;
import c.jahhow.remotecontroller.Msg.SCS1;

public class ArrowButtonFragment extends Fragment {
	MainActivity mainActivity;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		View layout = inflater.inflate(R.layout.button_controller, container, false);
		new LongPressAndUpDetector(layout.findViewById(R.id.buttonUp)) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Up_Arrow, ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Up_Arrow, ButtonAction.Up);
			}
		};
		new LongPressAndUpDetector(layout.findViewById(R.id.buttonLeft)) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Left_Arrow, ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Left_Arrow, ButtonAction.Up);
			}
		};
		new LongPressAndUpDetector(layout.findViewById(R.id.buttonRight)) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Right_Arrow, ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Right_Arrow, ButtonAction.Up);
			}
		};
		new LongPressAndUpDetector(layout.findViewById(R.id.buttonDown)) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Dn_Arrow, ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Dn_Arrow, ButtonAction.Up);
			}
		};
		return layout;
	}
}