package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class TurnOnBluetoothFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final BluetoothConnectorFragment bluetoothConnectorFragment = (BluetoothConnectorFragment) getParentFragment();
        View layout = inflater.inflate(R.layout.prompt_user_to_retry_to_enable_bluetooth, container, false);
        View btTurnOnBluetooth = layout.findViewById(R.id.buttonTurnOnBluetooth);
        btTurnOnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothConnectorFragment.TurnOnBluetooth();
            }
        });
        return layout;
    }
}
