package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;

import c.jahhow.remotecontroller.msg.ButtonAction;
import c.jahhow.remotecontroller.msg.InputTextMode;
import c.jahhow.remotecontroller.msg.SCS1;

public class InputTextFragment extends Fragment {
	TextInputEditText editText;
	MainActivity mainActivity;
	ImageView buttonToggleInputPassword;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();

		View layout = inflater.inflate(R.layout.input_text, container, false);
		editText = layout.findViewById(R.id.SendTextEditText);

		new LongPressAndUpDetector(layout.findViewById(R.id.inputTextButtonBackspace), mainActivity) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Backspace, ButtonAction.Down);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Backspace, ButtonAction.Up);
			}
		};

		new LongPressAndUpDetector(layout.findViewById(R.id.inputTextButtonEnter), mainActivity) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCode(SCS1.Enter, ButtonAction.Down);
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
		new LongPressAndUpDetector(buttonPasteText, mainActivity) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendInputText(editText.getText().toString(), InputTextMode.Paste, true);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCodeCombination(ButtonAction.Up, SCS1.L_CTRL, SCS1.V);
			}
		};

		new LongPressAndUpDetector(layout.findViewById(R.id.buttonCtrlV), mainActivity) {
			@Override
			void onLongClickDown(View v) {
				mainActivity.SendKeyboardScanCodeCombination(ButtonAction.Down, SCS1.L_CTRL, SCS1.V);
			}

			@Override
			void onLongClickUp(View v) {
				mainActivity.SendKeyboardScanCodeCombination(ButtonAction.Up, SCS1.L_CTRL, SCS1.V);
			}
		};

		buttonToggleInputPassword = layout.findViewById(R.id.buttonToggleInputPassword);
		buttonToggleInputPassword.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("RestrictedApi")
			@Override
			public void onClick(View v) {
				int inputType = editText.getInputType();
				if (isInputTypeNormal(inputType)) {
					inputType |= EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
					buttonToggleInputPassword.animate().alpha(1);
				} else {
					buttonToggleInputPassword.animate().alpha(.3f);
					inputType = inputType & ~EditorInfo.TYPE_MASK_VARIATION;
				}
				editText.setInputType(inputType);
			}
		});


		if (savedInstanceState == null)
			editText.setText(mainActivity.preferences.getString(MainActivity.KeyPrefer_InputText, null));
		else {
			int inputType = savedInstanceState.getInt(BundleKey_InputType, 0);
			if (inputType != 0) {
				editText.setInputType(inputType);
				if (!isInputTypeNormal(inputType))
					buttonToggleInputPassword.setAlpha(1f);
			}
		}
		return layout;
	}

	boolean isInputTypeNormal(int inputType) {
		return (inputType & EditorInfo.TYPE_MASK_VARIATION) == 0;
	}

	static final String BundleKey_InputType = "BKIT";

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		if (!mainActivity.isChangingConfigurations())
			mainActivity.preferences.edit().putString(MainActivity.KeyPrefer_InputText, editText.getText().toString()).apply();
		outState.putInt(BundleKey_InputType, editText.getInputType());
		//Log.e("InputTextFragment","onSaveInstanceState()");
	}

	@Override
	public void onDestroy() {
		//Log.e("InputTextFragment", "onDestroy()");
		super.onDestroy();
		if (!mainActivity.isChangingConfigurations())
			mainActivity.preferences.edit().putString(MainActivity.KeyPrefer_InputText, editText.getText().toString()).apply();
	}
}
