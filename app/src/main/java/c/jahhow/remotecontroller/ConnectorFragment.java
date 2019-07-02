package c.jahhow.remotecontroller;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ConnectorFragment extends Fragment {
	TextInputEditText tiEditTextIp, tiEditTextPort;
	Button buttonConnect;

	MainActivity mainActivity;
	Toast toast;
	MainViewModel mainViewModel;
	Fragment controller;
	int transactionCommitID;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		controller = new ControllerSwitcherFragment();
		mainActivity = (MainActivity) getActivity();
		mainViewModel = ViewModelProviders.of(mainActivity).get(MainViewModel.class);
		View view = inflater.inflate(R.layout.connector, container, false);
		tiEditTextIp = view.findViewById(R.id.editTextIp);
		tiEditTextPort = view.findViewById(R.id.editTextPort);
		buttonConnect = view.findViewById(R.id.buttonConnect);

		tiEditTextIp.setText(mainActivity.preferences.getString(MainActivity.KeyPrefer_IP, "192.168.1.3"));
		tiEditTextPort.setText(mainActivity.preferences.getString(MainActivity.KeyPrefer_Port, "5555"));
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		toast = mainActivity.toast;
	}

	@Override
	public void onPause() {
		super.onPause();
		mainActivity.preferences.edit()
				.putString(MainActivity.KeyPrefer_IP, tiEditTextIp.getText().toString())
				.putString(MainActivity.KeyPrefer_Port, tiEditTextPort.getText().toString())
				.apply();
	}

	static final byte Header[] = {'R', 'C', 'R', 'H'};
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
				mainViewModel.socket.connect(inetaddr, 5000);
				mainViewModel.socket.shutdownInput();
				mainViewModel.socketOutput = mainViewModel.socket.getOutputStream();
				mainViewModel.socketOutput.write(Header);
				mainActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						transactionCommitID = getFragmentManager().beginTransaction().addToBackStack(null)
								.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
								.remove(ConnectorFragment.this).add(android.R.id.content, controller).commit();
						buttonConnect.setEnabled(true);
					}
				});
			} catch (SocketTimeoutException e) {
				mainActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						buttonConnect.setEnabled(true);
						toast.setText("Timeout");
						toast.show();
					}
				});
				mainViewModel.socket = null;
			} catch (Exception e) {
				mainActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						buttonConnect.setEnabled(true);
						toast.setText("Connection Error");
						toast.show();
					}
				});
				mainViewModel.socket = null;
				Log.e("MainActivity", "Connection Error" + e.toString());
				e.printStackTrace();
			}
		}
	};
}
