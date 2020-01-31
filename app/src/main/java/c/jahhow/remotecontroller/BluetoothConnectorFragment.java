package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

public class BluetoothConnectorFragment extends MyFragment {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private MyBroadcastReceiver myBroadcastReceiver;
    private MainViewModel mainViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // MainActivity.onCreate() might have not been called.
        // onCreateView() might have not been called before onDetach()
        FragmentActivity activity = getActivity();
        assert activity != null;
        mainViewModel = new ViewModelProvider(activity).get(MainViewModel.class);
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
            myBroadcastReceiver = new MyBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            Context context = getContext();
            assert context != null;
            context.registerReceiver(myBroadcastReceiver, intentFilter);

            //Log.i(getClass().getSimpleName(), String.format("Child fragment %c= null", innerFragment == null ? '=' : '!'));
            if (mainViewModel.bluetoothConnectorFragment_showSelectBluetoothDeviceFragment || hasNoChildFragment()) {
                mainViewModel.bluetoothConnectorFragment_showSelectBluetoothDeviceFragment = false;
                //Log.i(getClass().getSimpleName(), "INIT");
                if (bluetoothAdapter.isEnabled()) {
                    replaceChildFragment(new SelectBluetoothDeviceFragment());
                } else {
                    TurnOnBluetooth();
                }
            }
            layout = inflater.inflate(R.layout.bluetooth_connector, container, false);
        }
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
            FragmentActivity activity = getActivity();
            assert activity != null;
            Context context = getContext();
            assert context != null;
            context.unregisterReceiver(myBroadcastReceiver);
            if (!mainViewModel.bluetoothOriginalState_isEnabled
                    && mainViewModel.bluetoothSocket == null
                    && !activity.isChangingConfigurations()) {
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

    void replaceChildFragment(Fragment newFragment) {
        getChildFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.bluetoothConnectorInnerFragmentContainer, newFragment).commit();
    }

    private void onBluetoothStateON() {
        replaceChildFragment(new SelectBluetoothDeviceFragment());
    }

    private void onBluetoothStateOFF() {
        replaceChildFragment(new TurnOnBluetoothFragment());
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                            case BluetoothAdapter.STATE_ON:
                                //Log.i(getClass().getSimpleName(), "BluetoothAdapter.STATE_ON");
                                onBluetoothStateON();
                                break;
                            case BluetoothAdapter.STATE_OFF:
                                //Log.i(getClass().getSimpleName(), "BluetoothAdapter.STATE_OFF");
                                onBluetoothStateOFF();
                                break;
                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                        //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                        int preBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, 0);
                        if (preBondState == BluetoothDevice.BOND_BONDING && bondState == BluetoothDevice.BOND_NONE) {
                            mainViewModel.bondingFailed = true;
                            replaceChildFragment(new BluetoothErrorFragment());
                        }
                        //Log.i(getClass().getSimpleName(), String.format("ACTION_BOND_STATE_CHANGED : %s %d => %d", device.getName(), preBondState, bondState));
                        break;
                    }
                }
            }
        }
    }
}
