package c.jahhow.remotecontroller;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TcpIpConnector implements ServerVerifier.ErrorCallback {
    private MainViewModel mainViewModel;
    SharedPreferences preferences;
    TcpIpConnectorFragment tcpIpConnectorFragment = null;

    TcpIpConnector(@NonNull MainViewModel mainViewModel, @NonNull SharedPreferences preferences) {
        this.mainViewModel = mainViewModel;
        this.preferences = preferences;
    }

    void connect(final String ip, final int port) {
        mainViewModel.socketHandlerThread = new HandlerThread("");
        mainViewModel.socketHandlerThread.start();
        mainViewModel.socketHandler = new Handler(mainViewModel.socketHandlerThread.getLooper());
        mainViewModel.socketHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    InetSocketAddress address = new InetSocketAddress(ip, port);
                    Socket mmSocket = new Socket();
                    mmSocket.setTcpNoDelay(true);
                    mmSocket.connect(address, 1500);
                    mmSocket.setSoTimeout(1500);
                    if (ServerVerifier.isValid(preferences, mainViewModel, mmSocket.getInputStream(),
                            mmSocket.getOutputStream(), TcpIpConnector.this)) {
                        mainViewModel.mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainViewModel.mainActivity.replaceFragment(new ControllerSwitcherFragment());
                                if (tcpIpConnectorFragment != null) {
                                    tcpIpConnectorFragment.onConnected();
                                }
                            }
                        });
                    }
                    preferences.edit().putBoolean(MainActivity.KeyPrefer_ShowTcpIpGuide, false).apply();
                } catch (SocketTimeoutException e) {
                    OnErrorConnecting(R.string.TimeoutCheckIpPortOrUpdate, Toast.LENGTH_LONG);
                } catch (Exception e) {
                    OnErrorConnecting(R.string.ConnectionError, Toast.LENGTH_SHORT);
                    //Log.e("MainActivity", R.string.ConnectionError + e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void OnErrorConnecting(final int showToast, final int duration) {
        mainViewModel.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tcpIpConnectorFragment != null) {
                    tcpIpConnectorFragment.OnErrorConnecting(showToast, duration);
                }
                mainViewModel.mainActivity.CloseConnection();
            }
        });
    }
}