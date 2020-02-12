package c.jahhow.remotecontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class TcpIpConnectorFragment extends MyFragment implements ServerVerifier.ErrorCallback {
    private static final String TAG = TcpIpConnectorFragment.class.getSimpleName();

    private TextInputEditText tiEditTextIp, tiEditTextPort;
    private Button buttonConnect;
    private ImageView buttonHelp;
    private LinearLayout connectButtonsParentLayout;

    private MainActivity mainActivity;
    private SharedPreferences preferences;
    private MainViewModel mainViewModel;

    // set buttons state on next onCreateView()
    private boolean setButtonsStateOnCreateView = false;
    private int helpButtonVisibility;
    private boolean connectButtonEnabled;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.connector, container, false);
        //Log.i(getClass().getSimpleName(), "onCreateView()");
		/*if (layoutIsPossiblyAttachedToWindow)
			Log.e(getClass().getSimpleName(), "layoutIsPossiblyAttachedToWindow == " + layoutIsPossiblyAttachedToWindow);*/
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        preferences = mainActivity.preferences;
        mainViewModel = mainActivity.mainViewModel;
        mainViewModel.tcpIpConnector.tcpIpConnectorFragment = this;

        tiEditTextIp = layout.findViewById(R.id.editTextIp);
        tiEditTextPort = layout.findViewById(R.id.editTextPort);
        buttonConnect = layout.findViewById(R.id.buttonConnect);
        buttonHelp = layout.findViewById(R.id.buttonHelp);
        connectButtonsParentLayout = layout.findViewById(R.id.connectButtonsParentLayout);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                mainViewModel.tcpIpConnector.connect(tiEditTextIp.getText().toString(),
                        Integer.parseInt(tiEditTextPort.getText().toString()));
            }
        });
        buttonHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowGuideTcpIp();
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

        if (isNotRestoringState()) {
            if (BuildConfig.DEBUG || preferences.getBoolean(MainActivity.KeyPrefer_ShowTcpIpGuide, true)) {
                ShowGuideTcpIp();
            }
        }
        return layout;
    }

    private void ShowGuideTcpIp() {
        getParentFragmentManager().beginTransaction()
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.ConnectorFragmentContainer, new GuideTcpIpConnectionFragment())
                .commit();
    }

    @Override
    public void onDestroyView() {
        //Log.i(getClass().getSimpleName(), "onDestroyView()");
        super.onDestroyView();
        if (!mainActivity.isChangingConfigurations())
            SavePreferences();
        mainViewModel.tcpIpConnector.tcpIpConnectorFragment = null;
    }

    /*@Override
    public void onDestroy() {
        //Log.i(getClass().getSimpleName(), "onDestroy()");
        super.onDestroy();
    }*/

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //Log.i(getClass().getSimpleName(), "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        SavePreferences();
    }

    private void SavePreferences() {
        if (buttonHelp != null) {
            int _helpButtonVisibility = setButtonsStateOnCreateView ? helpButtonVisibility : buttonHelp.getVisibility();
            //Log.i(getClass().getSimpleName(), "SavePreferences KeyPrefer_ShowHelpButton " + (_helpButtonVisibility == View.VISIBLE));
            preferences.edit()
                    .putString(MainActivity.KeyPrefer_IP, tiEditTextIp.getText().toString())
                    .putString(MainActivity.KeyPrefer_Port, tiEditTextPort.getText().toString())
                    .putBoolean(MainActivity.KeyPrefer_ShowHelpButton, _helpButtonVisibility == View.VISIBLE)
                    .apply();
        }
    }

    void onFoundServer(String ip, int port) {
        tiEditTextIp.setText(ip);
        tiEditTextPort.setText(String.valueOf(port));
        buttonConnect.performClick();
    }

    void onConnected() {
        setButtonsStateOnCreateView = true;
        connectButtonEnabled = true;
        helpButtonVisibility = View.GONE;
    }

    public void OnErrorConnecting(@StringRes final int showToast, final int toastDuration) {
        TransitionManager.beginDelayedTransition(connectButtonsParentLayout, new AutoTransition()
                .setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500)
                .setOrdering(TransitionSet.ORDERING_TOGETHER));
        buttonHelp.setVisibility(View.VISIBLE);
        if (!isRemoving()) {
            buttonConnect.setEnabled(true);
            mainActivity.ShowToast(showToast, toastDuration);
        }
    }
}