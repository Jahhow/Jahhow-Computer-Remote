package c.jahhow.remotecontroller;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.ArrayAdapter;

import androidx.lifecycle.ViewModel;

import java.io.OutputStream;

// ViewModel must be public.
public class MainViewModel extends ViewModel {
    MainActivity mainActivity = null;
    BluetoothConnectorFragment bluetoothConnectorFragment = null;
    HandlerThread socketHandlerThread = null;
    Handler socketHandler = null;
    OutputStream outputStream = null;
    ArrayAdapter<BluetoothDevice> nearbyBTArrayAdapter = null;
    boolean hasSet_bluetoothOriginalState = false;
    boolean bluetoothOriginalState_isEnabled;
    boolean bluetoothConnectorFragment_showSelectBluetoothDeviceFragment = false;
    boolean bondingFailed = false;
    boolean doAutoTcpConnect=true;
}