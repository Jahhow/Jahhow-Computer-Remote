package c.jahhow.remotecontroller;

import android.arch.lifecycle.ViewModel;

import java.io.OutputStream;
import java.net.Socket;

public class MainViewModel extends ViewModel {
	Socket socket = null;
	OutputStream socketOutput = null;

	String inputText = null;
}
