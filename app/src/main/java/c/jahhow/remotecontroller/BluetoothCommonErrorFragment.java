package c.jahhow.remotecontroller;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class BluetoothCommonErrorFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth_common_error, container, false);
        view.findViewById(R.id.buttonOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothConnectorFragment bluetoothConnectorFragment = (BluetoothConnectorFragment) getParentFragment();
                assert bluetoothConnectorFragment != null;
                bluetoothConnectorFragment.replaceChildFragment(new SelectBluetoothDeviceFragment());
            }
        });
        return view;
    }
}