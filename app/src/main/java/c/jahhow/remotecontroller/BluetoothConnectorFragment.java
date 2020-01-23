package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class BluetoothConnectorFragment extends Fragment {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private MyBroadcastReceiver myBroadcastReceiver;
    private boolean bluetoothOriginalState_isEnabled;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout;
        if (bluetoothAdapter == null) {
            layout = inflater.inflate(R.layout.no_bluetooth, container, false);
            myBroadcastReceiver = null;
        } else {
            bluetoothOriginalState_isEnabled = bluetoothAdapter.isEnabled();
            myBroadcastReceiver = new MyBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getContext().registerReceiver(myBroadcastReceiver, intentFilter);
            if (!bluetoothAdapter.enable()) {
                Toast.makeText(getContext(), "Error turning on Bluetooth", Toast.LENGTH_SHORT).show();
            }

            layout = inflater.inflate(R.layout.fragment_bluetooth_connector, container, false);
        }
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(myBroadcastReceiver);
        if (bluetoothAdapter != null) {
            if (!bluetoothOriginalState_isEnabled) {
                bluetoothAdapter.disable();
            }
        }
    }
}
