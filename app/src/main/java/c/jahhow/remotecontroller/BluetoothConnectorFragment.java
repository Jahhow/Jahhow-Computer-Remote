package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BluetoothConnectorFragment extends Fragment {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(getClass().getSimpleName(), bluetoothAdapter == null ? "bluetoothAdapter==null" : "bluetoothAdapter!=null");
        return inflater.inflate(R.layout.fragment_bluetooth_connector, container, false);
    }
}
