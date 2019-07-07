package c.jahhow.remotecontroller;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import c.jahhow.remotecontroller.Msg.ButtonAction;
import c.jahhow.remotecontroller.Msg.InputTextMode;
import c.jahhow.remotecontroller.Msg.SCS1;

public class SendTextFragment extends Fragment {
	TextInputEditText editText;
	MainActivity mainActivity;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();

		View layout = inflater.inflate(R.layout.input_text, container, false);
		editText = layout.findViewById(R.id.SendTextEditText);
		if (mainActivity.mainViewModel.inputText != null)
			editText.setText(mainActivity.mainViewModel.inputText);
		else
			editText.setText(mainActivity.preferences.getString(MainActivity.KeyPrefer_InputText, null));

		new LongPressAndUpDetector(layout.findViewById(R.id.inputTextButtonBackspace)) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Backspace, ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Backspace, ButtonAction.Up);
			}
		};

		new LongPressAndUpDetector(layout.findViewById(R.id.inputTextButtonEnter)) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Enter,ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Enter, ButtonAction.Up);
			}
		};

		View buttonSend = layout.findViewById(R.id.buttonSend);
		buttonSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mainActivity.SendInputText(editText.getText().toString(), InputTextMode.SendInput, false);
			}
		});

		View buttonPasteText = layout.findViewById(R.id.buttonPasteText);
		buttonPasteText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mainActivity.SendInputText(editText.getText().toString(), InputTextMode.Paste, false);
			}
		});
		new LongPressAndUpDetector(buttonPasteText) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendInputText(editText.getText().toString(), InputTextMode.Paste, true);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCodeCombination(ButtonAction.Up, SCS1.L_CTRL, SCS1.V);
			}
		};
		return layout;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mainActivity.mainViewModel.inputText = editText.getText().toString();
		mainActivity.preferences.edit().putString(MainActivity.KeyPrefer_InputText, mainActivity.mainViewModel.inputText).apply();
	}
}
