package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import c.jahhow.remotecontroller.msg.ButtonAction;
import c.jahhow.remotecontroller.msg.InputTextMode;
import c.jahhow.remotecontroller.msg.SCS1;

public class InputTextFragment extends Fragment {
    private static final String
            BundleKey_InputType = "BKIT",
            BundleKey_ShowingHelp = "BKSH";

    private MainActivity mainActivity;
    private TextInputEditText editText;
    private ImageView buttonToggleInputPassword;
    private View helpLayout;
    private boolean showHelp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();

        View layout = inflater.inflate(R.layout.input_text_switcher, container, false);
        editText = layout.findViewById(R.id.SendTextEditText);

        View backspace = layout.findViewById(R.id.inputTextButtonBackspace);
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.SendKeyboardScanCode(SCS1.Backspace, ButtonAction.Click);
            }
        });
        new LongPressAndUpDetector(backspace, mainActivity) {
            @Override
            void onLongClickDown(View v) {
                showHelp = false;
                mainActivity.SendKeyboardScanCode(SCS1.Backspace, ButtonAction.Down);
            }

            @Override
            void onLongClickUp(View v) {
                mainActivity.SendKeyboardScanCode(SCS1.Backspace, ButtonAction.Up);
            }
        };

        View enter = layout.findViewById(R.id.inputTextButtonEnter);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.SendKeyboardScanCode(SCS1.Enter, ButtonAction.Click);
            }
        });
        new LongPressAndUpDetector(enter, mainActivity) {
            @Override
            void onLongClickDown(View v) {
                showHelp = false;
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
                showHelp = false;
                mainActivity.SendInputText(editText.getText().toString(), InputTextMode.SendInput, false);
            }
        });

        View buttonPasteText = layout.findViewById(R.id.buttonPasteText);
        buttonPasteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelp = false;
                mainActivity.SendInputText(editText.getText().toString(), InputTextMode.Paste, false);
            }
        });
        new LongPressAndUpDetector(buttonPasteText, mainActivity) {
            @Override
            void onLongClickDown(View v) {
                showHelp = false;
                mainActivity.SendInputText(editText.getText().toString(), InputTextMode.Paste, true);
            }

            @Override
            void onLongClickUp(View v) {
                mainActivity.SendKeyboardScanCodeCombination(ButtonAction.Up, SCS1.L_CTRL, SCS1.V);
            }
        };

        View buttonPaste = layout.findViewById(R.id.buttonCtrlV);
        buttonPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_CTRL, SCS1.V);
            }
        });
        new LongPressAndUpDetector(buttonPaste, mainActivity) {
            @Override
            void onLongClickDown(View v) {
                showHelp = false;
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

        showHelp = mainActivity.preferences.getBoolean(MainActivity.KeyPrefer_ShowHelpInputText, true);

        boolean showHelpHere;
        if (savedInstanceState == null) {
            editText.setText(mainActivity.preferences.getString(MainActivity.KeyPrefer_InputText, null));
            showHelpHere = showHelp;
        } else {
            int inputType = savedInstanceState.getInt(BundleKey_InputType, 0);
            if (inputType != 0) {
                editText.setInputType(inputType);
                if (!isInputTypeNormal(inputType))
                    buttonToggleInputPassword.setAlpha(1f);
            }
            showHelpHere = savedInstanceState.getBoolean(BundleKey_ShowingHelp);
        }

        helpLayout = layout.findViewById(R.id.helpInputTextLayout);
        if (showHelpHere) {

                /* It seems that xml animateLayoutChanges can only perform one visibility change
                     at a time for both FrameLayout and ConstraintLayout.
                   That is, the first setVisibility() would have no animation performed.
                   So I end up adding non-transparent background color under help_input_text.xml,
                     then animate only help_input_text's visibility instead of both.
                */

            //Log.i(getClass().getSimpleName(), "Manually Showing Help Layout");
            //final View inputTextLayout = layout.findViewById(R.id.inputTextLayout);

            //inputTextLayout.setVisibility(View.GONE);
            helpLayout.setVisibility(View.VISIBLE);

            View buttonOk = helpLayout.findViewById(R.id.buttonOk);
            buttonOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //inputTextLayout.setVisibility(View.VISIBLE);
                    helpLayout.setVisibility(View.GONE);
                }
            });
        }
        return layout;
    }

    private boolean isInputTypeNormal(int inputType) {
        return (inputType & EditorInfo.TYPE_MASK_VARIATION) == 0;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mainActivity.isChangingConfigurations()) {
            mainActivity.preferences.edit()
                    .putString(MainActivity.KeyPrefer_InputText, editText.getText().toString())
                    .putBoolean(MainActivity.KeyPrefer_ShowHelpInputText, showHelp).apply();
        }
        outState.putInt(BundleKey_InputType, editText.getInputType());
        outState.putBoolean(BundleKey_ShowingHelp, helpLayout.getVisibility() == View.VISIBLE);
        //Log.i("InputTextFragment","onSaveInstanceState()");
    }

    @Override
    public void onDestroyView() {
        //Log.i("InputTextFragment", "onDestroy()");
        super.onDestroyView();
        if (!mainActivity.isChangingConfigurations()) {
            mainActivity.preferences.edit()
                    .putString(MainActivity.KeyPrefer_InputText, editText.getText().toString())
                    .putBoolean(MainActivity.KeyPrefer_ShowHelpInputText, showHelp).apply();
        }
    }
}