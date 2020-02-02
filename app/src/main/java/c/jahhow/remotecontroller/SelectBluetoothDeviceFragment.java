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
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class SelectBluetoothDeviceFragment extends Fragment implements AdapterView.OnItemClickListener, ServerVerifier.ErrorCallback {
    MainActivity mainActivity;
    private MainViewModel mainViewModel;
    private BluetoothConnectorFragment bluetoothConnectorFragment;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothBroadcastReceiver bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
    private ProgressBar progressBar;
    private Button scanButton;

    private IntentFilter intentFilter = new IntentFilter();
    private static final short PERMISSION_REQUEST_CODE = 8513;
    private static final UUID BT_SERVICE_UUID = UUID.fromString("C937E0B7-8C64-C221-4A25-F40120B3064E");

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
                BluetoothDevice item = getItem(position);
                assert item != null;
                ((TextView) view.getChildAt(0)).setText(item.getName());
                ((TextView) view.getChildAt(1)).setText(item.getAddress());
                return view;
            }
        };
        pairedBTListView.setAdapter(arrayAdapter);
        pairedBTListView.setOnItemClickListener(this);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetoothDiscovery();
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

    @Override
    public void onStart() {
        super.onStart();
        //Log.i(getClass().getSimpleName(), "onStart()");
        if (mainViewModel.nearbyBTArrayAdapter.isEmpty())
            startBluetoothDiscovery();
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
                startBluetoothDiscovery();
            }
        }
    }

    private void startBluetoothDiscovery() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Log.i(getClass().getSimpleName(), "bluetoothAdapter.startDiscovery()");
            bluetoothAdapter.startDiscovery();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mainViewModel.bondingFailed = false;
        bluetoothConnectorFragment.replaceChildFragment(new LoadingFragment(getText(R.string.connecting)));
        BluetoothDevice bluetoothDevice = (BluetoothDevice) parent.getItemAtPosition(position);
        mainViewModel.socketHandlerThread = new HandlerThread("");
        mainViewModel.socketHandlerThread.start();
        mainViewModel.socketHandler = new Handler(mainViewModel.socketHandlerThread.getLooper());
        mainViewModel.socketHandler.post(new BluetoothConnectRunnable(bluetoothDevice));
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
        private final BluetoothSocket mmSocket;

        BluetoothConnectRunnable(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(BT_SERVICE_UUID);
            } catch (IOException e) {
                //Log.e(getClass().getSimpleName(), "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                mainViewModel.bluetoothSocket = mmSocket;
                if (ServerVerifier.isValid(mainActivity.preferences, mainViewModel, mmSocket.getInputStream(), mmSocket.getOutputStream(), SelectBluetoothDeviceFragment.this)) {
                    mainViewModel.bluetoothConnectorFragment_showSelectBluetoothDeviceFragment = true;
                    mainViewModel.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainViewModel.mainActivity.getSupportFragmentManager().beginTransaction().addToBackStack(null)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                    .replace(android.R.id.content, new ControllerSwitcherFragment()).commitAllowingStateLoss();
                        }
                    });
                }
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException ignored) {
                }
                OnErrorConnecting(R.string.ConnectionError);
                //Log.e(getClass().getSimpleName(), "IOException: " + connectException);
            } catch (Exception e) {
                OnErrorConnecting(R.string.ConnectionError);
                //Log.e(getClass().getSimpleName(), "final catch " + e);
            }
        }
    }

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

    public void OnErrorConnecting(@StringRes final int showToast) {
        OnErrorConnecting(showToast, Toast.LENGTH_SHORT);
    }
}
