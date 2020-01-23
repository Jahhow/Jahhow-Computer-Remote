package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {
    BluetoothConnectorFragment bluetoothConnectorFragment;

    MyBroadcastReceiver(BluetoothConnectorFragment bluetoothConnectorFragment) {
        this.bluetoothConnectorFragment = bluetoothConnectorFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
            case BluetoothAdapter.STATE_ON:
                Log.i(getClass().getSimpleName(), "BluetoothAdapter.STATE_ON");
                bluetoothConnectorFragment.onBluetoothStateON();
                break;
            case BluetoothAdapter.STATE_OFF:
                Log.i(getClass().getSimpleName(), "BluetoothAdapter.STATE_OFF");
                bluetoothConnectorFragment.onBluetoothStateOFF();
                break;
        }
    }
}
