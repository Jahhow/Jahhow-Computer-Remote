package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase.PurchaseState;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import c.jahhow.remotecontroller.msg.ButtonAction;
import c.jahhow.remotecontroller.msg.Msg;
import c.jahhow.remotecontroller.msg.SCS1;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();
    private static final String ReceiverProgramWebsite = "http://jahhowapp.blogspot.com/2019/07/computer-remote-controller.html#receiver-program";

    static final String name_CommonSharedPrefer = "CommonSettings",
            KeyPrefer_IP = "0",
            KeyPrefer_Port = "1",
            KeyPrefer_Controller = "2",
            KeyPrefer_SwipeDemo = "3",
            KeyPrefer_Swiped = "4",
            KeyPrefer_InputText = "5",
            KeyPrefer_VibrateOnDown = "6",
            KeyPrefer_ShowTcpIpHelpButton = "7",
            KeyPrefer_ShowMainHelp = "8",
            KeyPrefer_ShowHelpInputText = "9",
            KeyPrefer_ShowTcpIpGuide = "a",
            KeyPrefer_Connector = "b",
            KeyPrefer_SuccessfulConnectionCount = "c";

    RemoteControllerApp remoteControllerApp;
    MainViewModel mainViewModel;
    static SharedPreferences preferences;

    private Toast toast;
    Vibrator vibrator;

    private final RemoteControllerApp.FetchSkuListener fetchSkuListener = new RemoteControllerApp.FetchSkuListener() {
        @Override
        public void onSkuDetailsResponse() {
            if (remoteControllerApp.skuDetails == null) {
                Toast.makeText(getApplicationContext(), R.string.FailedToReachGooglePlay, Toast.LENGTH_SHORT).show();
            } else {
                remoteControllerApp.billingClient.launchBillingFlow(MainActivity.this, BillingFlowParams.newBuilder().setSkuDetails(remoteControllerApp.skuDetails).build());
            }
        }
    };

    public void OnClick_ManagePlaySubs(MenuItem v) {
        if (remoteControllerApp.purchaseState == PurchaseState.UNSPECIFIED_STATE) {
            // launchBillingFlow
            if (remoteControllerApp.skuDetails == null) {
                remoteControllerApp.setFetchSkuListener(fetchSkuListener);
                remoteControllerApp.SyncPurchase();
                if (remoteControllerApp.purchaseState != PurchaseState.UNSPECIFIED_STATE) {
                    remoteControllerApp.OpenPlayStoreManageSubscription(this);
                }
            } else {
                remoteControllerApp.billingClient.launchBillingFlow(this, BillingFlowParams.newBuilder().setSkuDetails(remoteControllerApp.skuDetails).build());
            }
        } else {
            remoteControllerApp.OpenPlayStoreManageSubscription(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.mainActivity = this;
        if (preferences == null)
            preferences = getSharedPreferences(name_CommonSharedPrefer, MODE_PRIVATE);
        remoteControllerApp = (RemoteControllerApp) getApplication();
        if (mainViewModel.tcpIpConnector == null) {
            mainViewModel.tcpIpConnector = new TcpIpConnector(mainViewModel);
        }

        vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        if (vibrator != null && !vibrator.hasVibrator()) vibrator = null;
        toast = Toast.makeText(this, null, Toast.LENGTH_SHORT);
        if (savedInstanceState == null) {
            Fragment fragment;
            if (/*BuildConfig.DEBUG || */preferences.getBoolean(KeyPrefer_ShowMainHelp, true)) {
                fragment = new MainHelpFragment();
                startAutoTcpConnect();
            } else
                fragment = new ConnectorSwitcherFragment();
            replaceFragment(fragment);
        }
    }

    @Override
    protected void onDestroy() {
        remoteControllerApp.removeFetchSkuListener(fetchSkuListener);
        if (!isChangingConfigurations()) {
            stopAutoTcpConnect();
        }
        super.onDestroy();
    }

    void stopAutoTcpConnect() {
        if (mainViewModel.udpSocket != null) {
            mainViewModel.udpSocket.close();
            mainViewModel.udpSocket = null;
        }
    }

    void startAutoTcpConnect() {
        if (!mainViewModel.doAutoTcpConnect || mainViewModel.udpSocket != null)
            return;
        try {
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifi == null)
                return;

            int wifiIP = Integer.reverseBytes(wifi.getConnectionInfo().getIpAddress());
            if (wifiIP == 0)
                return;

            byte[] broadcastIpBytes = ByteBuffer.allocate(4).putInt(wifiIP | 0xFF).array();

            InetAddress broadcastIP = InetAddress.getByAddress(broadcastIpBytes);// x.x.255.255 failed
            int broadcastPort = 1597;
            mainViewModel.udpSocket = new DatagramSocket(broadcastPort, broadcastIP);// must specify a port
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
                        mainViewModel.udpSocket.receive(packet);
                    } catch (IOException e) {
                        break;
                    }
                    final int port = ServerVerifier.getTcpPort(packet);
                    if (port > 0) {
                        final String senderIP = packet.getAddress().getHostAddress();
                        //Log.i(TAG, "Found server at " + senderIP + ":" + port);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mainViewModel.tcpIpConnector.tcpIpConnectorFragment != null)
                                    mainViewModel.tcpIpConnector.tcpIpConnectorFragment.onFoundServer(senderIP, port);
                                else {
                                    preferences.edit()
                                            .putBoolean(KeyPrefer_Connector, ConnectorSwitcherFragment.PreferTcpIp)
                                            .putBoolean(MainActivity.KeyPrefer_ShowTcpIpHelpButton, false)
                                            .putString(KeyPrefer_IP, senderIP)
                                            .putString(KeyPrefer_Port, String.valueOf(port)).apply();
                                    mainViewModel.tcpIpConnector.connect(senderIP, port);
                                }
                                stopAutoTcpConnect();
                                mainViewModel.doAutoTcpConnect = false;
                            }
                        });
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
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().getFragments().get(0);
        if (fragment != null) {
            if (fragment instanceof ControllerSwitcherFragment) {
                replaceFragment(new ConnectorSwitcherFragment());
                return;
            } else if (fragment instanceof ConnectorSwitcherFragment) {
                if (fragment.getChildFragmentManager().popBackStackImmediate())
                    return;
            }
        }
        super.onBackPressed();
    }

    public void OpenJahhowAppWebsite(View ignored) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ReceiverProgramWebsite)));
    }

    void replaceFragment(Fragment newFragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(android.R.id.content, newFragment).commitAllowingStateLoss();
        /*List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Log.i(TAG, "fragments.size(): " + fragments.size());*/
    }

    void Vibrate(long ms) {
        if (ms > 0 && vibrator != null)
            vibrator.vibrate(ms);
    }

    public void Vibrate() {
        Vibrate(30);
    }

    public void SendClick_Left(View v) {
        SendKeyboardScanCode(SCS1.Left_Arrow, ButtonAction.Click);
    }

    public void SendClick_Up(View v) {
        SendKeyboardScanCode(SCS1.Up_Arrow, ButtonAction.Click);
    }

    public void SendClick_Right(View v) {
        SendKeyboardScanCode(SCS1.Right_Arrow, ButtonAction.Click);
    }

    public void SendClick_Down(View v) {
        SendKeyboardScanCode(SCS1.Dn_Arrow, ButtonAction.Click);
    }

    public void SendClick_Esc(View v) {
        SendKeyboardScanCode(SCS1.Esc, ButtonAction.Click);
    }

    public void SendClick_Home(View v) {
        SendKeyboardScanCode(SCS1.Home, ButtonAction.Click);
    }

    public void SendClick_End(View v) {
        SendKeyboardScanCode(SCS1.End, ButtonAction.Click);
    }

    public void SendClick_F5(View v) {
        SendKeyboardScanCode(SCS1.F5, ButtonAction.Click);
    }

    public void SendClick_ShiftF5(View v) {
        SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_SHIFT, SCS1.F5);
    }

    public void SendClick_CtrlC(View v) {
        SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_CTRL, SCS1.C);
    }

    public void SendClick_CtrlA(View v) {
        SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_CTRL, SCS1.A);
    }

    public void SendClick_CtrlX(View v) {
        SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_CTRL, SCS1.X);
    }

    public void SendMouseMove(short dx, short dy) {
        if (mainViewModel.socketHandler == null)
            return;

        final byte[] bytes = ByteBuffer.allocate(5).put(Msg.MoveMouse).putShort(dx).putShort(dy).array();
        mainViewModel.socketHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mainViewModel.outputStream.write(bytes);
                    //mainViewModel.outputStream.flush();
                } catch (IOException e) {
                    OnSendCommandError();
                }
            }
        });
    }

    public void SendMouseWheel(int amount) {
        final byte[] bytes = ByteBuffer.allocate(5).put(Msg.MouseWheel).putInt(amount).array();
        mainViewModel.socketHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mainViewModel.outputStream.write(bytes);
                } catch (IOException e) {
                    OnSendCommandError();
                }
            }
        });
    }

    private static final String SendTextEncode = "UTF-16LE";

    // mode = InputTextMode.{SendInput, Paste}
    public void SendInputText(final String text, byte mode, boolean Hold) {
        if (mainViewModel.socketHandler == null || text.length() == 0)
            return;

        try {
            byte[] textBytes = text.getBytes(SendTextEncode);
            int textByteLen = textBytes.length;
            //Log.i("SendInputText", String.format("text.length() = %d", text.length()));
            //Log.i("SendInputText", String.format("textBytes.length = %d", textBytes.length));
            final byte[] packet = ByteBuffer.allocate(7 + textByteLen)
                    .put(Msg.InputText)
                    .putInt(textByteLen)
                    .put(mode)
                    .put((byte) (Hold ? 1 : 0))
                    .put(textBytes).array();

            mainViewModel.socketHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mainViewModel.outputStream.write(packet);
                    } catch (IOException e) {
                        OnSendCommandError();
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            ShowToast(R.string.ProblemSendingText, Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
    }

    public void SendMouseLeftDown() {
        SendMsg(Msg.MouseLeftDown);
    }

    public void SendMouseLeftUp() {
        SendMsg(Msg.MouseLeftUp);
    }

    public void SendMouseLeftClick() {
        SendMsg(Msg.MouseLeftClick);
    }

    public void SendMouseRightDown() {
        SendMsg(Msg.MouseRightDown);
    }

    public void SendMouseRightUp() {
        SendMsg(Msg.MouseRightUp);
    }

    public void SendMouseRightClick() {
        SendMsg(Msg.MouseRightClick);
    }

    private void SendMsg(final byte msg) {
        if (mainViewModel.socketHandler != null)
            mainViewModel.socketHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mainViewModel.outputStream.write(msg);
                    } catch (IOException e) {
                        OnSendCommandError();
                    }
                }
            });
    }

    public void SendKeyboardScanCode(short ScanCode, byte buttonAction) {
        if (mainViewModel.socketHandler == null)
            return;

        final byte[] bytes = ByteBuffer.allocate(4)
                .put(Msg.KeyboardScanCode)
                .putShort(ScanCode)
                .put(buttonAction)
                .array();
        mainViewModel.socketHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mainViewModel.outputStream.write(bytes);
                } catch (IOException e) {
                    OnSendCommandError();
                }
            }
        });
    }

    // actionType: ButtonAction.{Click, Down, or Up}
    public void SendKeyboardScanCodeCombination(final byte actionType, final short... ScanCodes) {
        if (mainViewModel.socketHandler == null)
            return;

        int scanCodeByteLen = ScanCodes.length << 1;
        ByteBuffer byteBuffer = ByteBuffer.allocate(3 + scanCodeByteLen)
                .put(Msg.KeyboardScanCodeCombination)
                .put(actionType)
                .put((byte) scanCodeByteLen);
        for (short scanCode : ScanCodes)
            byteBuffer.putShort(scanCode);

        final byte[] packet = byteBuffer.array();
        mainViewModel.socketHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mainViewModel.outputStream.write(packet);
                } catch (IOException e) {
                    OnSendCommandError();
                }
            }
        });
    }

    void ShowToast(@StringRes int resId, int duration) {
        ShowToast(getString(resId), duration);
    }

    private void ShowToast(String text, int duration) {
        toast.setText(text);
        toast.setDuration(duration);
        toast.show();
    }

    private void OnSendCommandError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getSupportFragmentManager().isStateSaved()) {
                    replaceFragment(new ConnectorSwitcherFragment());
                }
                if (!isFinishing()) {
                    ShowToast(R.string.Disconnected, Toast.LENGTH_SHORT);
                }
                CloseConnection();
            }
        });
    }

    // Call it on UI Thread
    void CloseConnection() {
        if (mainViewModel.socketHandler != null) {
            mainViewModel.socketHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mainViewModel.outputStream != null)
                        try {
                            mainViewModel.outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    mainViewModel.socketHandlerThread.quit();

                    mainViewModel.socketHandlerThread = null;
                    mainViewModel.socketHandler = null;
                    mainViewModel.outputStream = null;
                }
            });
        }
    }
}