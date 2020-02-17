package c.jahhow.remotecontroller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class SelectBluetoothDeviceFragment extends Fragment implements AdapterView.OnItemClickListener, ServerVerifier.ErrorCallback {
    static final String TAG = SelectBluetoothDeviceFragment.class.getSimpleName();

    private MainActivity mainActivity;
    private MainViewModel mainViewModel;
    private RemoteControllerApp remoteControllerApp;
    private BluetoothConnectorFragment bluetoothConnectorFragment;

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BluetoothBroadcastReceiver bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
    private ProgressBar progressBar;
    private Button scanButton;

    private final IntentFilter intentFilter = new IntentFilter();
    private static final short PERMISSION_REQUEST_CODE = 8513;
    private static final UUID BT_SERVICE_UUID = new UUID(0xC937E0B78C64C221L, 0x4A25F40120B3064EL);
    private LeHidDevice leHidDevice;
    private HidDevice hidDevice;

    public SelectBluetoothDeviceFragment() {
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.i(getClass().getSimpleName(), "onCreateView()");
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mainViewModel = mainActivity.mainViewModel;
        remoteControllerApp = mainActivity.remoteControllerApp;
        bluetoothConnectorFragment = (BluetoothConnectorFragment) getParentFragment();

        View layout = inflater.inflate(R.layout.select_bluetooth_device, container, false);
        ListView pairedBTListView = layout.findViewById(R.id.listSavedBluetoothDevice);
        ListView nearbyBTListView = layout.findViewById(R.id.listBluetoothDeviceNearby);
        progressBar = layout.findViewById(R.id.progressBarBTScanning);
        scanButton = layout.findViewById(R.id.btScanButton);

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        ArrayAdapter<BluetoothDevice> arrayAdapter = new ArrayAdapter<BluetoothDevice>(mainActivity, R.layout.nearby_bluetooth_device, new ArrayList<>(bondedDevices)) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LinearLayout view = (LinearLayout) convertView;
                if (view == null) {
                    view = (LinearLayout) inflater.inflate(R.layout.nearby_bluetooth_device, parent, false);
                }
                BluetoothDevice bluetoothDevice = getItem(position);
                assert bluetoothDevice != null;
                ((TextView) view.getChildAt(0)).setText(bluetoothDevice.getName());
                ((TextView) view.getChildAt(1)).setText(bluetoothDevice.getAddress());
                return view;
            }
        };
        pairedBTListView.setAdapter(arrayAdapter);
        pairedBTListView.setOnItemClickListener(this);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetoothDiscovery(true);
            }
        });
        if (mainViewModel.nearbyBTArrayAdapter == null) {
            mainViewModel.nearbyBTArrayAdapter = new ArrayAdapter<BluetoothDevice>(mainActivity, R.layout.nearby_bluetooth_device) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    LinearLayout view = (LinearLayout) convertView;
                    if (view == null) {
                        view = (LinearLayout) inflater.inflate(R.layout.nearby_bluetooth_device, parent, false);
                    }
                    BluetoothDevice item = getItem(position);
                    assert item != null;
                    ((TextView) view.getChildAt(0)).setText(item.getName());
                    ((TextView) view.getChildAt(1)).setText(item.getAddress());
                    return view;
                }
            };
        }
        nearbyBTListView.setAdapter(mainViewModel.nearbyBTArrayAdapter);
        nearbyBTListView.setOnItemClickListener(this);

        if (bluetoothAdapter.isDiscovering()) {
            scanButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
        mainActivity.registerReceiver(bluetoothBroadcastReceiver, intentFilter);
        return layout;
    }

    boolean demo = true;
    BluetoothDevice device;

    @Override
    public void onStart() {
        super.onStart();
        //Log.i(getClass().getSimpleName(), "onStart()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                hidDevice = new HidDevice(mainActivity);
                hidDevice.openServer();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "demo start");
                        byte y = 3;
                        while (demo) {
                            if (device != null)
                                hidDevice.sendMouseMove((byte) 0, y = (byte) -y);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        Log.i(TAG, "demo stop");
                    }
                }).start();
            } else {
                leHidDevice = new LeHidDevice(mainActivity);
                leHidDevice.listener = new LeHidDevice.Listener() {
                    @Override
                    public void onConnected(BluetoothDevice device) {
                        Log.i(TAG, "onConnected " + device);
                    }

                    @Override
                    public void onDisconnected(BluetoothDevice device) {
                        Log.i(TAG, "onDisconnected " + device);
                    }
                };
                leHidDevice.openServer();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "demo start");
                        byte y = 3;
                        while (demo) {
                            if (device != null)
                                leHidDevice.sendMouseMove(device, (byte) 0, y = (byte) -y);
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        Log.i(TAG, "demo stop");
                    }
                }).start();
            }
        }
        if (mainViewModel.nearbyBTArrayAdapter.isEmpty())
            startBluetoothDiscovery(false);
    }

    @SuppressLint("NewApi")
    @Override
    public void onStop() {
        super.onStop();
        if (leHidDevice != null) {
            leHidDevice.closeServer();
        } else if (hidDevice != null) {
            hidDevice.closeServer();
        }
        demo = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!mainActivity.isChangingConfigurations()) {
            //Log.i(getClass().getSimpleName(), "bluetoothAdapter.cancelDiscovery()");
            bluetoothAdapter.cancelDiscovery();
        }
        mainActivity.unregisterReceiver(bluetoothBroadcastReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothDiscovery(false);
            }
        }
    }

    private void startBluetoothDiscovery(boolean force) {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Log.i(getClass().getSimpleName(), "bluetoothAdapter.startDiscovery()");
            bluetoothAdapter.startDiscovery();
        } else {
            if (force || remoteControllerApp.requestLocationPermission) {
                remoteControllerApp.requestLocationPermission = false;
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
        if (leHidDevice != null) {
            this.device = device;
            if (!leHidDevice.connect(device)) {
                mainActivity.ShowToast(R.string.ConnectionError, Toast.LENGTH_SHORT);
            }
        } else if (hidDevice != null) {
            this.device = device;
            if (!hidDevice.connect(device)) {
                mainActivity.ShowToast(R.string.ConnectionError, Toast.LENGTH_SHORT);
            }
        } else {
            mainViewModel.bondingFailed = false;
            bluetoothConnectorFragment.replaceChildFragment(new LoadingFragment(getText(R.string.connecting)));
            mainViewModel.socketHandlerThread = new HandlerThread("");
            mainViewModel.socketHandlerThread.start();
            mainViewModel.socketHandler = new Handler(mainViewModel.socketHandlerThread.getLooper());
            mainViewModel.socketHandler.post(new BluetoothConnectRunnable(device));
        }
    }

    class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothDevice.ACTION_FOUND: {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        mainViewModel.nearbyBTArrayAdapter.add(device);
                        break;
                    }
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        //Log.i(getClass().getSimpleName(), "ACTION_DISCOVERY_STARTED");
                        mainViewModel.nearbyBTArrayAdapter.clear();
                        scanButton.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        //Log.i(getClass().getSimpleName(), "ACTION_DISCOVERY_FINISHED");
                        scanButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }

    private class BluetoothConnectRunnable implements Runnable {
        private final BluetoothSocket bluetoothSocket;

        BluetoothConnectRunnable(BluetoothDevice device) {
            // Use a temporary object that is later assigned to bluetoothSocket
            // because bluetoothSocket is final.
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(BT_SERVICE_UUID);
            } catch (IOException e) {
                //Log.e(getClass().getSimpleName(), "Socket's create() method failed", e);
            }
            bluetoothSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                bluetoothSocket.connect();
                if (ServerVerifier.isValid(mainViewModel, bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream(), SelectBluetoothDeviceFragment.this)) {
                    mainViewModel.bluetoothConnectorFragment_showSelectBluetoothDeviceFragment = true;
                    mainViewModel.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainViewModel.mainActivity.replaceFragment(new ControllerSwitcherFragment());
                        }
                    });
                }
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    bluetoothSocket.close();
                } catch (IOException ignored) {
                }
                OnErrorConnecting(R.string.ConnectionError, Toast.LENGTH_SHORT);
                //Log.e(getClass().getSimpleName(), "IOException: " + connectException);
            } catch (Exception e) {
                OnErrorConnecting(R.string.ConnectionError, Toast.LENGTH_SHORT);
                //Log.e(getClass().getSimpleName(), "final catch " + e);
            }
        }
    }

    @Override
    public void OnErrorConnecting(@StringRes final int showToast, final int duration) {
        mainViewModel.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mainViewModel.mainActivity, showToast, duration).show();
                if (mainViewModel.bluetoothConnectorFragment != null) {
                    if (mainViewModel.bondingFailed) {
                        mainViewModel.bondingFailed = false;
                    } else {
                        mainViewModel.bluetoothConnectorFragment.replaceChildFragment(
                                new BluetoothCommonErrorFragment());
                    }
                }
                mainViewModel.mainActivity.CloseConnection();
            }
        });
    }
}