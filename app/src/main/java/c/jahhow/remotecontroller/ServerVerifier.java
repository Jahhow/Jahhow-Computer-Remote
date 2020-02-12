package c.jahhow.remotecontroller;

import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

class ServerVerifier {
    static private final byte[] Header = {'R', 'C', 'R', 'H'};
    static private final byte[] ServerHeader = {'U', 'E', 'R', 'J'};
    static private final int SupportServerVersion = 2;

    private ServerVerifier() {
    }

    static boolean isValid(SharedPreferences preferences, MainViewModel mainViewModel, InputStream inputStream, OutputStream outputStream, ErrorCallback errorCallback) throws IOException {
        mainViewModel.outputStream = outputStream;
        mainViewModel.outputStream.write(Header);

        byte[] buf = new byte[ServerHeader.length];
        if (ServerHeader.length != inputStream.read(buf, 0, ServerHeader.length)) {
            errorCallback.OnErrorConnecting(R.string.ConnectionError, Toast.LENGTH_SHORT);
            return false;
        }
        if (!Arrays.equals(buf, ServerHeader)) {
            errorCallback.OnErrorConnecting(R.string.ConnectionError, Toast.LENGTH_SHORT);
            return false;
        }
        if (4 != inputStream.read(buf, 0, 4)) {
            errorCallback.OnErrorConnecting(R.string.PleaseUpdateTheComputerSideReceiverProgram, Toast.LENGTH_LONG);
            return false;
        }
        int serverVersion = ByteBuffer.wrap(buf).getInt();
        if (serverVersion < SupportServerVersion) {
            errorCallback.OnErrorConnecting(R.string.PleaseUpdateTheComputerSideReceiverProgram, Toast.LENGTH_LONG);
            return false;
        } else if (serverVersion > SupportServerVersion) {
            errorCallback.OnErrorConnecting(R.string.PleaseUpdateThisApp, Toast.LENGTH_SHORT);
            return false;
        }
        preferences.edit().putBoolean(MainActivity.KeyPrefer_ShowHelpOnCreate, false).apply();
        return true;
    }

    static final int BROADCAST_DATA_LENGTH = 2 + ServerHeader.length + 4;
    static final int
            ERROR_InvalidPacket = 0,
            ERROR_PleaseUpdateTheComputerSideReceiverProgram = -1,
            ERROR_PleaseUpdateThisApp = -2;

    static int getTcpPort(DatagramPacket packet) {
        byte[] data = packet.getData();
        if (data.length != BROADCAST_DATA_LENGTH)
            return ERROR_InvalidPacket;

        for (int i = 0; i < ServerHeader.length; ++i) {
            if (data[i + 2] != ServerHeader[i])
                return ERROR_InvalidPacket;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        int port = (int) byteBuffer.getShort() & 0xFFFF;
        int serverVersion = byteBuffer.getInt(2 + ServerHeader.length);

        if (serverVersion < SupportServerVersion) {
            return ERROR_PleaseUpdateTheComputerSideReceiverProgram;
        } else if (serverVersion > SupportServerVersion) {
            return ERROR_PleaseUpdateThisApp;
        }
        return port;
    }

    interface ErrorCallback {
        void OnErrorConnecting(@StringRes final int showToast, final int duration);
    }
}