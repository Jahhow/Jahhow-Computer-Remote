package c.jahhow.remotecontroller;

import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.OutputStream;
import java.net.Socket;

public class MainViewModel extends ViewModel {
	HandlerThread socketHandlerThread = null;
	Handler socketHandler = null;
	Socket socket = null;
	OutputStream socketOutput = null;

	String inputText = null;
}
