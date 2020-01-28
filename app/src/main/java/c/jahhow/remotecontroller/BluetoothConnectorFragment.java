package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

public class BluetoothConnectorFragment extends Fragment {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private MyBroadcastReceiver myBroadcastReceiver;
    private MainViewModel mainViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // MainActivity.onCreate() might have not been called.
        // onCreateView() might have not been called before onDetach()
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        mainViewModel.bluetoothConnectorFragment = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout;
        if (bluetoothAdapter == null) {
            layout = inflater.inflate(R.layout.no_bluetooth, container, false);
            myBroadcastReceiver = null;
        } else {
            if (!mainViewModel.hasSet_bluetoothOriginalState) {
                mainViewModel.hasSet_bluetoothOriginalState = true;
                mainViewModel.bluetoothOriginalState_isEnabled = bluetoothAdapter.isEnabled();
            }
            myBroadcastReceiver = new MyBroadcastReceiver(this);
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getContext().registerReceiver(myBroadcastReceiver, intentFilter);
            if (savedInstanceState == null) {
                if (bluetoothAdapter.isEnabled()) {
                    replaceChildFragment(new SelectBluetoothDeviceFragment());
                } else {
                    TurnOnBluetooth();
                }
            }
            layout = inflater.inflate(R.layout.fragment_bluetooth_connector, container, false);
        }
        Log.i(getClass().getSimpleName(), String.format("savedInstanceState %c= null", savedInstanceState == null ? '=' : '!'));
        return layout;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainViewModel.bluetoothConnectorFragment = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bluetoothAdapter != null) {
            getContext().unregisterReceiver(myBroadcastReceiver);
            if (!mainViewModel.bluetoothOriginalState_isEnabled && mainViewModel.bluetoothSocket != null
                    && !getActivity().isChangingConfigurations()) {
                bluetoothAdapter.disable();
            }
        }
    }

    void TurnOnBluetooth() {
        if (bluetoothAdapter.enable()) {
            replaceChildFragment(new LoadingFragment());
        } else {
            replaceChildFragment(new TurnOnBluetoothFragment());
        }
    }

    void onBluetoothStateON() {
        replaceChildFragment(new SelectBluetoothDeviceFragment());
    }

    void onBluetoothStateOFF() {
        replaceChildFragment(new TurnOnBluetoothFragment());
    }

    void replaceChildFragment(Fragment newFragment) {
        getChildFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.bluetoothConnectorInnerFragmentContainer, newFragment).commit();
    }
}
