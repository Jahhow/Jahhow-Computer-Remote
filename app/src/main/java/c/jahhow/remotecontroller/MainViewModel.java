package c.jahhow.remotecontroller;

import androidx.lifecycle.ViewModel;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.ArrayAdapter;

import java.io.OutputStream;
import java.net.Socket;

public class MainViewModel extends ViewModel {
    HandlerThread socketHandlerThread = null;
    Handler socketHandler = null;
    Socket socket = null;
    BluetoothSocket bluetoothSocket = null;
    OutputStream socketOutput = null;
    ArrayAdapter<BluetoothDevice> nearbyBTArrayAdapter = null;
    boolean hasSet_bluetoothOriginalState = false;
    boolean bluetoothOriginalState_isEnabled;
}