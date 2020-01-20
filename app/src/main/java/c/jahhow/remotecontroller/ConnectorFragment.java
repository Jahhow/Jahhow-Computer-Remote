package c.jahhow.remotecontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ConnectorFragment extends Fragment {

    private RemoteControllerApp remoteControllerApp;
    private TextInputEditText tiEditTextIp, tiEditTextPort;
    private Button buttonConnect;
    private ImageView buttonHelp;
    private LinearLayout connectButtonsParentLayout;

    MainActivity mainActivity;
    private SharedPreferences preferences;
    private MainViewModel mainViewModel;
    private ControllerSwitcherFragment controllersFragment;

    // set buttons state on next onCreateView()
    private boolean setButtonsStateOnCreateView = false;
    private int helpButtonVisibility;
    private boolean connectButtonEnabled;

	/*@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		Log.i(getClass().getSimpleName(), "onCreate()");
		super.onCreate(savedInstanceState);
	}*/

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.connector, container, false);
        //Log.i(getClass().getSimpleName(), "onCreateView()");
		/*if (layoutIsPossiblyAttachedToWindow)
			Log.e(getClass().getSimpleName(), "layoutIsPossiblyAttachedToWindow == " + layoutIsPossiblyAttachedToWindow);*/

        controllersFragment = new ControllerSwitcherFragment();
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        preferences = mainActivity.preferences;
        remoteControllerApp = (RemoteControllerApp) mainActivity.getApplication();
        mainViewModel = mainActivity.mainViewModel;

        tiEditTextIp = layout.findViewById(R.id.editTextIp);
        tiEditTextPort = layout.findViewById(R.id.editTextPort);
        buttonConnect = layout.findViewById(R.id.buttonConnect);
        buttonHelp = layout.findViewById(R.id.buttonHelp);
        connectButtonsParentLayout = layout.findViewById(R.id.connectButtonsParentLayout);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                mainViewModel.socketHandlerThread = new HandlerThread("");
                mainViewModel.socketHandlerThread.start();
                mainViewModel.socketHandler = new Handler(mainViewModel.socketHandlerThread.getLooper());
                mainViewModel.socketHandler.post(connectRunnable);
            }
        });

        if (savedInstanceState == null) {
            tiEditTextIp.setText(preferences.getString(MainActivity.KeyPrefer_IP, "192.168.1.3"));
            tiEditTextPort.setText(preferences.getString(MainActivity.KeyPrefer_Port, "1597"));
        }

        if (setButtonsStateOnCreateView) {
            setButtonsStateOnCreateView = false;
            buttonConnect.setEnabled(connectButtonEnabled);
            buttonHelp.setVisibility(helpButtonVisibility);
        } else {
            //Log.i(getClass().getSimpleName(), "preferences.getBoolean(KeyPrefer_ShowHelpButton, true) == " + preferences.getBoolean(MainActivity.KeyPrefer_ShowHelpButton, true));
            buttonHelp.setVisibility(preferences.getBoolean(MainActivity.KeyPrefer_ShowHelpButton, true) ? View.VISIBLE : View.GONE);
        }
        return layout;
    }

    private void SavePreferences() {
        int _helpButtonVisibility = setButtonsStateOnCreateView ? helpButtonVisibility : buttonHelp.getVisibility();
        //Log.i(getClass().getSimpleName(), "SavePreferences KeyPrefer_ShowHelpButton " + (_helpButtonVisibility == View.VISIBLE));
        preferences.edit()
                .putString(MainActivity.KeyPrefer_IP, tiEditTextIp.getText().toString())
                .putString(MainActivity.KeyPrefer_Port, tiEditTextPort.getText().toString())
                .putBoolean(MainActivity.KeyPrefer_ShowHelpButton, _helpButtonVisibility == View.VISIBLE)
                .apply();
    }

	/*@Override
	public void onDestroyView() {
		Log.e(getClass().getSimpleName(), "onDestroyView()");
		super.onDestroyView();
	}*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        remoteControllerApp.fetchFullAccessSkuListener = null;
        if (!mainActivity.isChangingConfigurations())
            SavePreferences();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        SavePreferences();
    }


    // Run it on Ui Thread
    private void AnimateShowHelpButton() {
        TransitionManager.beginDelayedTransition(connectButtonsParentLayout, new AutoTransition().setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500).setOrdering(TransitionSet.ORDERING_TOGETHER));
        buttonHelp.setVisibility(View.VISIBLE);
    }

    private void OnErrorConnecting(@StringRes final int showToast, final int toastDuration) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AnimateShowHelpButton();
                if (!isRemoving()) {
                    buttonConnect.setEnabled(true);
                    mainActivity.ShowToast(showToast, toastDuration);
                }
                mainActivity.CloseConnection();
            }
        });
    }

    private void OnErrorConnecting(@StringRes final int showToast) {
        OnErrorConnecting(showToast, Toast.LENGTH_SHORT);
    }

    private static final byte[] Header = {'R', 'C', 'R', 'H'};
    private static final byte[] ServerHeader = {'U', 'E', 'R', 'J'};
    private static final int SupportServerVersion = 1;
    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                InetSocketAddress inetaddr = new InetSocketAddress(
                        tiEditTextIp.getText().toString(),
                        Integer.parseInt(tiEditTextPort.getText().toString())
                );
                mainViewModel.socket = new Socket();
                mainViewModel.socket.setTcpNoDelay(true);
                mainViewModel.socket.connect(inetaddr, 1500);
                mainViewModel.socketOutput = mainViewModel.socket.getOutputStream();
                mainViewModel.socketOutput.write(Header);

                mainViewModel.socket.setSoTimeout(1500);
                InputStream inputStream = mainViewModel.socket.getInputStream();
                byte[] buf = new byte[ServerHeader.length];
                if (ServerHeader.length != inputStream.read(buf, 0, ServerHeader.length)) {
                    OnErrorConnecting(R.string.ConnectionError);
                    return;
                }
                if (!Arrays.equals(buf, ServerHeader)) {
                    OnErrorConnecting(R.string.ConnectionError);
                    return;
                }
                if (4 != inputStream.read(buf, 0, 4)) {
                    OnErrorConnecting(R.string.PleaseUpdateTheComputerSideReceiverProgram, Toast.LENGTH_LONG);
                    return;
                }
                int serverVersion = ByteBuffer.wrap(buf).getInt();
                if (serverVersion < SupportServerVersion) {
                    OnErrorConnecting(R.string.PleaseUpdateTheComputerSideReceiverProgram, Toast.LENGTH_LONG);
                    return;
                } else if (serverVersion > SupportServerVersion) {
                    OnErrorConnecting(R.string.PleaseUpdateThisApp);
                    return;
                }
                mainViewModel.socket.shutdownInput();
                mainActivity.runOnUiThread(runnableOpenControllerFragment);
            } catch (SocketTimeoutException e) {
                OnErrorConnecting(R.string.TimeoutCheckIPportOrUpdate, Toast.LENGTH_LONG);
            } catch (Exception e) {
                OnErrorConnecting(R.string.ConnectionError);
                //Log.e("MainActivity", R.string.ConnectionError + e.toString());
                e.printStackTrace();
            }
        }
    };

    // On Ui Thread
    private Runnable runnableOpenControllerFragment = new Runnable() {
        @Override
        public void run() {
            mainActivity.getSupportFragmentManager().saveFragmentInstanceState(
                    mainActivity.getSupportFragmentManager().getFragments().get(0)
            );
            mainActivity.getSupportFragmentManager().beginTransaction().addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(android.R.id.content, controllersFragment).commit();

            setButtonsStateOnCreateView = true;
            connectButtonEnabled = true;
            helpButtonVisibility = View.GONE;

            preferences.edit().putBoolean(MainActivity.KeyPrefer_ShowHelpOnCreate, false).apply();
        }
    };
}
