package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
            myBroadcastReceiver = new MyBroadcastReceiver(this);
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getContext().registerReceiver(myBroadcastReceiver, intentFilter);
            if (bluetoothAdapter.isEnabled()) {
                replaceChildFragment(new SelectBluetoothDeviceFragment());
            } else {
                TurnOnBluetooth();
            }
            layout = inflater.inflate(R.layout.fragment_bluetooth_connector, container, false);
        }
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bluetoothAdapter != null) {
            getContext().unregisterReceiver(myBroadcastReceiver);
            if (!bluetoothOriginalState_isEnabled) {
                bluetoothAdapter.disable();
            }
        }
    }

    void TurnOnBluetooth() {
        if (bluetoothAdapter.enable()) {
            replaceChildFragment(new LoadingFragment());
        } else {
            replaceChildFragment(new TurnOnBluetoothFragment(this));
        }
    }

    void onBluetoothStateON() {
        replaceChildFragment(new SelectBluetoothDeviceFragment());
    }

    void onBluetoothStateOFF() {
        replaceChildFragment(new TurnOnBluetoothFragment(this));
    }

    void replaceChildFragment(Fragment newFragment) {
        getChildFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.bluetoothConnectorInnerFragmentContainer, newFragment).commit();
    }
}
