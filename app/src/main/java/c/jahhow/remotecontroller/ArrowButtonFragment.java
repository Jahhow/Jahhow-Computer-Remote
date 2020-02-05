package c.jahhow.remotecontroller;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import c.jahhow.remotecontroller.msg.ButtonAction;
import c.jahhow.remotecontroller.msg.SCS1;

class ArrowButtonFragment extends Fragment {
	private MainActivity mainActivity;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		View layout = inflater.inflate(R.layout.arrow_buttons, container, false);
		new LongPressAndUpDetector(layout.findViewById(R.id.buttonUp), mainActivity) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Up_Arrow, ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Up_Arrow, ButtonAction.Up);
			}
		};
		new LongPressAndUpDetector(layout.findViewById(R.id.buttonLeft), mainActivity) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Left_Arrow, ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Left_Arrow, ButtonAction.Up);
			}
		};
		new LongPressAndUpDetector(layout.findViewById(R.id.buttonRight), mainActivity) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Right_Arrow, ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Right_Arrow, ButtonAction.Up);
			}
		};
		new LongPressAndUpDetector(layout.findViewById(R.id.buttonDown), mainActivity) {
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