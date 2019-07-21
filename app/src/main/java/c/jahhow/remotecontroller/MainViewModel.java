package c.jahhow.remotecontroller;

import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;

import java.io.OutputStream;
import java.net.Socket;

public class MainViewModel extends ViewModel {
	HandlerThread socketHandlerThread = null;
	Handler socketHandler = null;
	Socket socket = null;
	OutputStream socketOutput = null;

	//int helpButtonVisibility = View.GONE;
}