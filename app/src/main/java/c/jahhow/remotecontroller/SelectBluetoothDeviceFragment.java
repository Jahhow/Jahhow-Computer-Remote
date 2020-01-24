package c.jahhow.remotecontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static c.jahhow.remotecontroller.TcpIpConnectorFragment.Header;
import static c.jahhow.remotecontroller.TcpIpConnectorFragment.ServerHeader;
import static c.jahhow.remotecontroller.TcpIpConnectorFragment.SupportServerVersion;

public class SelectBluetoothDeviceFragment extends Fragment {
    MainActivity mainActivity;
    MainViewModel mainViewModel;
    BluetoothConnectorFragment bluetoothConnectorFragment;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDiscoveryBroadcastReceiver bluetoothDiscoveryBroadcastReceiver = new BluetoothDiscoveryBroadcastReceiver();
    private ArrayAdapter<BluetoothDevice> nearbyBTArrayAdapter;
    private ProgressBar progressBar;
    private Button scanButton;

    private static final short PERMISSION_REQUEST_CODE = 8513;
    private static final UUID BT_SERVICE_UUID = new UUID(0xC937E0B78C64C221L, 0x4A25F40120B3064EL);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        mainViewModel = mainActivity.mainViewModel;
        bluetoothConnectorFragment = (BluetoothConnectorFragment) getParentFragment();
        View layout = inflater.inflate(R.layout.fragment_select_bluetooth_device, container, false);
        ListView pairedBTListView = layout.findViewById(R.id.listSavedBluetoothDevice);
        ListView nearbyBTListView = layout.findViewById(R.id.listBluetoothDeviceNearby);
        progressBar = layout.findViewById(R.id.progressBarBTScanning);
        scanButton = layout.findViewById(R.id.btScanButton);

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        ArrayAdapter<BluetoothDevice> arrayAdapter = new ArrayAdapter<BluetoothDevice>(getContext(), R.layout.bluetooth_device, new ArrayList<>(bondedDevices)) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) convertView;
                if (view == null) {
                    view = (TextView) getLayoutInflater().inflate(R.layout.bluetooth_device, parent, false);
                }
                view.setText(getItem(position).getName());
                return view;
            }
        };
        pairedBTListView.setAdapter(arrayAdapter);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(bluetoothDiscoveryBroadcastReceiver, intentFilter);
        startBluetoothDiscovery();

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetoothDiscovery();
            }
        });
        nearbyBTArrayAdapter = new ArrayAdapter<BluetoothDevice>(getContext(), R.layout.bluetooth_device) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LinearLayout view = (LinearLayout) convertView;
                if (view == null) {
                    view = (LinearLayout) getLayoutInflater().inflate(R.layout.nearby_bluetooth_device, parent, false);
                }
                ((TextView) view.getChildAt(0)).setText(getItem(position).getName());
                ((TextView) view.getChildAt(1)).setText(getItem(position).getAddress());
                return view;
            }
        };
        nearbyBTListView.setAdapter(nearbyBTArrayAdapter);
        nearbyBTListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.i(SelectBluetoothDeviceFragment.class.getSimpleName(), " getParentFragment() => " + getParentFragment().getClass().getSimpleName());
                bluetoothConnectorFragment.replaceChildFragment(new LoadingFragment("Connecting"));
                BluetoothDevice bluetoothDevice = (BluetoothDevice) parent.getItemAtPosition(position);
                mainViewModel.socketHandlerThread = new HandlerThread("");
                mainViewModel.socketHandlerThread.start();
                mainViewModel.socketHandler = new Handler(mainViewModel.socketHandlerThread.getLooper());
                mainViewModel.socketHandler.post(new ConnectRunnable(bluetoothDevice));
            }
        });

        return layout;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothDiscovery();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(bluetoothDiscoveryBroadcastReceiver);
    }

    private void startBluetoothDiscovery() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.startDiscovery();
        } else {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    class BluetoothDiscoveryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.i(getClass().getSimpleName(), "BluetoothDevice Found : " + device.getName());
                    nearbyBTArrayAdapter.add(device);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.i(getClass().getSimpleName(), "ACTION_DISCOVERY_STARTED");
                    nearbyBTArrayAdapter.clear();
                    scanButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.i(getClass().getSimpleName(), "ACTION_DISCOVERY_FINISHED");
                    scanButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private class ConnectRunnable implements Runnable {
        private final BluetoothSocket mmSocket;

        ConnectRunnable(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(BT_SERVICE_UUID);
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        boolean timeout;

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();
            timeout = false;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.i("timeoutThread", e.toString());
                    }
                    if (!mmSocket.isConnected()) {
                        try {
                            timeout = true;
                            mmSocket.close();
                        } catch (IOException ignored) {
                        }
                        Log.i("timeoutThread", "mmSocket.isConnected() => false");
                    } else {
                        Log.i("timeoutThread", "mmSocket.isConnected() => true");
                    }
                }
            }).start();

            try {
                mmSocket.connect();
                Log.e(getClass().getSimpleName(), "connected");
                if (mainActivity != null) {
                    MainViewModel mainViewModel = ViewModelProviders.of(mainActivity).get(MainViewModel.class);
                    mainViewModel.bluetoothSocket = mmSocket;
                    mainViewModel.socketOutput = mmSocket.getOutputStream();
                    mainViewModel.socketOutput.write(Header);

                    Log.e(getClass().getSimpleName(), "write(Header);");
                    InputStream inputStream = mmSocket.getInputStream();
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
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.getSupportFragmentManager().beginTransaction().addToBackStack(null)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                    .replace(android.R.id.content, new ControllerSwitcherFragment()).commitAllowingStateLoss();
                        }
                    });
                }
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                if (timeout) {
                    OnErrorConnecting(R.string.bluetooth_timeout);
                } else {
                    try {
                        mmSocket.close();
                    } catch (IOException ignored) {
                    }
                    OnErrorConnecting(R.string.ConnectionError);
                    Log.e(getClass().getSimpleName(), "IOException: " + connectException);
                }
            } catch (Exception e) {
                OnErrorConnecting(R.string.ConnectionError);
                Log.e(getClass().getSimpleName(), "final catch " + e);
            }
        }
    }

    private void OnErrorConnecting(@StringRes final int showToast, final int duration) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isRemoving()) {
                    Toast.makeText(mainActivity, showToast, duration).show();
                }
                bluetoothConnectorFragment.replaceChildFragment(SelectBluetoothDeviceFragment.this);
                mainActivity.CloseConnection();
            }
        });
    }

    private void OnErrorConnecting(@StringRes final int showToast) {
        OnErrorConnecting(showToast, Toast.LENGTH_SHORT);
    }
}
