package c.jahhow.remotecontroller;

import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BluetoothErrorFragment extends Fragment {
    public BluetoothErrorFragment() {
    }

    BluetoothErrorFragment(@StringRes int resId) {
        Bundle bundle = new Bundle();
        bundle.putInt(null, resId);
        setArguments(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth_error, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            int resId = arguments.getInt(null);
            TextView text = view.findViewById(R.id.btErrorText);
            text.setText(resId);
        }
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
