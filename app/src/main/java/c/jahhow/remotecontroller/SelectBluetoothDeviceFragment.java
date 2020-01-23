package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class SelectBluetoothDeviceFragment extends Fragment {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDiscoveryBroadcastReceiver bluetoothDiscoveryBroadcastReceiver = new BluetoothDiscoveryBroadcastReceiver();
    private ArrayAdapter<BluetoothDevice> nearbyBTArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_select_bluetooth_device, container, false);
        ListView pairedBTListView = layout.findViewById(R.id.listSavedBluetoothDevice);
        ListView nearbyBTListView = layout.findViewById(R.id.listBluetoothDeviceNearby);

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
        bluetoothAdapter.startDiscovery();

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

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().unregisterReceiver(bluetoothDiscoveryBroadcastReceiver);
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
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.i(getClass().getSimpleName(), "ACTION_DISCOVERY_FINISHED");
                    nearbyBTArrayAdapter.clear();
                    break;
            }
        }
    }
}
