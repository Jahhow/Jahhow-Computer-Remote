package c.jahhow.remotecontroller;

import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            errorCallback.OnErrorConnecting(R.string.ConnectionError);
            return false;
        }
        if (!Arrays.equals(buf, ServerHeader)) {
            errorCallback.OnErrorConnecting(R.string.ConnectionError);
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
            errorCallback.OnErrorConnecting(R.string.PleaseUpdateThisApp);
            return false;
        }
        preferences.edit().putBoolean(MainActivity.KeyPrefer_ShowHelpOnCreate, false).apply();
        return true;
    }

    interface ErrorCallback {
        void OnErrorConnecting(@StringRes final int showToast, final int duration);

        void OnErrorConnecting(@StringRes final int showToast);
    }
}
