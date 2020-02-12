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
    private ControllerSwitcherFragment controllersFragment;

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

        controllersFragment = new ControllerSwitcherFragment();
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        preferences = mainActivity.preferences;
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
        if (mainViewModel.doAutoTcpConnect) {
            listenForUdpBroadcast();
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

    private DatagramSocket socket = null;

    private void listenForUdpBroadcast() {
        try {
            WifiManager wifi = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi == null)
                return;

            int wifiIP = Integer.reverseBytes(wifi.getConnectionInfo().getIpAddress());
            if (wifiIP == 0)
                return;

            byte[] broadcastIpBytes = ByteBuffer.allocate(4).putInt(wifiIP | 0xFF).array();

            InetAddress broadcastIP = InetAddress.getByAddress(broadcastIpBytes);// x.x.255.255 failed
            int broadcastPort = 1597;
            socket = new DatagramSocket(broadcastPort, broadcastIP);// must specify a port
            //Log.i(TAG, "Broadcast address:" + broadcastIP + ":" + broadcastPort);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket packet = new DatagramPacket(new byte[ServerVerifier.BROADCAST_DATA_LENGTH], ServerVerifier.BROADCAST_DATA_LENGTH);
                //Log.i(TAG, "Listening UDB Broadcast");
                while (true) {
                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        break;
                    }
                    final int port = ServerVerifier.getTcpPort(packet);
                    if (port > 0) {
                        final String senderIP = packet.getAddress().getHostAddress();
                        //Log.i(TAG, "Found server at " + senderIP + ":" + port);
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tiEditTextIp.setText(senderIP);
                                tiEditTextPort.setText(String.valueOf(port));
                                buttonConnect.performClick();
                                socket.close();
                                socket = null;// do this on UI thread is safer
                            }
                        });
                        mainViewModel.doAutoTcpConnect = false;
                        break;
                    }
                    //else
                    //    Log.i(TAG, "getTcpPort error: " + port);

                    //String data = new String(packet.getData());
                    //Log.i(TAG, "Got UDB broadcast from " + senderIP + " \"" + data + "\", {" + Arrays.toString(packet.getData()) + "}");
                }
                //Log.i(TAG, "Stopped Listening UDB Broadcast");
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        //Log.i(getClass().getSimpleName(), "onDestroyView()");
        super.onDestroyView();
        if (!mainActivity.isChangingConfigurations())
            SavePreferences();

        if (socket != null) {
            socket.close();// use this to stop the thread
            socket = null;
        }
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

    // Run it on Ui Thread
    private void AnimateShowHelpButton() {
        TransitionManager.beginDelayedTransition(connectButtonsParentLayout, new AutoTransition().setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500).setOrdering(TransitionSet.ORDERING_TOGETHER));
        buttonHelp.setVisibility(View.VISIBLE);
    }

    public void OnErrorConnecting(@StringRes final int showToast, final int toastDuration) {
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

    public void OnErrorConnecting(@StringRes final int showToast) {
        OnErrorConnecting(showToast, Toast.LENGTH_SHORT);
    }

    private final Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                InetSocketAddress inetaddr = new InetSocketAddress(
                        tiEditTextIp.getText().toString(),
                        Integer.parseInt(tiEditTextPort.getText().toString())
                );
                Socket mmSocket = new Socket();
                mmSocket.setTcpNoDelay(true);
                mmSocket.connect(inetaddr, 1500);
                mmSocket.setSoTimeout(1500);
                if (ServerVerifier.isValid(preferences, mainViewModel, mmSocket.getInputStream(),
                        mmSocket.getOutputStream(), TcpIpConnectorFragment.this))
                    mainActivity.runOnUiThread(runnableOpenControllerFragment);
                preferences.edit().putBoolean(MainActivity.KeyPrefer_ShowTcpIpGuide, false).apply();
            } catch (SocketTimeoutException e) {
                OnErrorConnecting(R.string.TimeoutCheckIpPortOrUpdate, Toast.LENGTH_LONG);
            } catch (Exception e) {
                OnErrorConnecting(R.string.ConnectionError);
                //Log.e("MainActivity", R.string.ConnectionError + e.toString());
                e.printStackTrace();
            }
        }
    };

    // On Ui Thread
    private final Runnable runnableOpenControllerFragment = new Runnable() {
        @Override
        public void run() {
            mainActivity.replaceFragment(controllersFragment);
            setButtonsStateOnCreateView = true;
            connectButtonEnabled = true;
            helpButtonVisibility = View.GONE;
        }
    };
}