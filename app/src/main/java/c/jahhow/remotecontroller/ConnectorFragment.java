package c.jahhow.remotecontroller;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ConnectorFragment extends Fragment {
	RemoteControllerApp remoteControllerApp;
	TextInputEditText tiEditTextIp, tiEditTextPort;
	Button buttonConnect;

	MainActivity mainActivity;
	MainViewModel mainViewModel;
	ControllerSwitcherFragment controllersFragment;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		controllersFragment = new ControllerSwitcherFragment();
		mainActivity = (MainActivity) getActivity();
		remoteControllerApp = (RemoteControllerApp) mainActivity.getApplication();
		mainActivity.connectorFragment = this;
		mainViewModel = mainActivity.mainViewModel;
		View view = inflater.inflate(R.layout.connector, container, false);
		tiEditTextIp = view.findViewById(R.id.editTextIp);
		tiEditTextPort = view.findViewById(R.id.editTextPort);
		buttonConnect = view.findViewById(R.id.buttonConnect);

		tiEditTextIp.setText(mainActivity.preferences.getString(MainActivity.KeyPrefer_IP, "192.168.1.3"));
		tiEditTextPort.setText(mainActivity.preferences.getString(MainActivity.KeyPrefer_Port, "5555"));
		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		remoteControllerApp.fetchFullAccessSkuListener = null;
		mainActivity.preferences.edit()
				.putString(MainActivity.KeyPrefer_IP, tiEditTextIp.getText().toString())
				.putString(MainActivity.KeyPrefer_Port, tiEditTextPort.getText().toString())
				.apply();
	}

	static final byte[] Header = {'R', 'C', 'R', 'H'};
	static final byte[] ServerHeader = {'U', 'E', 'R', 'J'};
	static final int SupportServerVersion = 1;
	public Runnable connectRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				InetSocketAddress inetaddr = new InetSocketAddress(
						tiEditTextIp.getText().toString(),
						Integer.parseInt(tiEditTextPort.getText().toString())
				);
				mainViewModel.socket = new Socket();
				mainViewModel.socket.setTcpNoDelay(true);
				mainViewModel.socket.connect(inetaddr, 1500);
				mainViewModel.socketOutput = mainViewModel.socket.getOutputStream();
				mainViewModel.socketOutput.write(Header);

				mainViewModel.socket.setSoTimeout(1500);
				InputStream inputStream = mainViewModel.socket.getInputStream();
				byte[] buf = new byte[ServerHeader.length];
				if (ServerHeader.length != inputStream.read(buf, 0, ServerHeader.length)) {
					mainActivity.OnSocketError(getString(R.string.ConnectionError));
					return;
				}
				if (!Arrays.equals(buf, ServerHeader)) {
					mainActivity.OnSocketError(getString(R.string.ConnectionError));
					return;
				}
				if (4 != inputStream.read(buf, 0, 4)) {
					mainActivity.OnSocketError(getString(R.string.PleaseUpdateTheComputerSideReceiverProgram), Toast.LENGTH_LONG);
					return;
				}
				int serverVersion = ByteBuffer.wrap(buf).getInt();
				if (serverVersion < SupportServerVersion) {
					mainActivity.OnSocketError(getString(R.string.PleaseUpdateTheComputerSideReceiverProgram), Toast.LENGTH_LONG);
					return;
				} else if (serverVersion > SupportServerVersion) {
					mainActivity.OnSocketError(getString(R.string.PleaseUpdateThisApp));
					return;
				}
				mainViewModel.socket.shutdownInput();
				mainActivity.runOnUiThread(runnableOpenControllerFragment);
			} catch (SocketTimeoutException e) {
				mainActivity.OnSocketError(getString(R.string.TimeoutCheckIPportOrUpdate), Toast.LENGTH_LONG);
			} catch (Exception e) {
				mainActivity.OnSocketError(getString(R.string.ConnectionError));
				Log.e("MainActivity", getString(R.string.ConnectionError) + e.toString());
				e.printStackTrace();
			}
		}
	};

	Runnable runnableOpenControllerFragment = new Runnable() {
		@Override
		public void run() {
			mainActivity.getSupportFragmentManager().beginTransaction().addToBackStack(null)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.replace(android.R.id.content, controllersFragment).commit();
		}
	};
}
