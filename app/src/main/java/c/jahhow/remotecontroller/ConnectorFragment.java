package c.jahhow.remotecontroller;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ConnectorFragment extends Fragment {
	View layout = null;

	RemoteControllerApp remoteControllerApp;
	TextInputEditText tiEditTextIp, tiEditTextPort;
	Button buttonConnect;
	AppCompatImageView buttonHelp;
	LinearLayout connectButtonsParentLayout;

	MainActivity mainActivity;
	SharedPreferences preferences;
	MainViewModel mainViewModel;
	ControllerSwitcherFragment controllersFragment;

	// set buttons state on next onCreateView()
	boolean postDelayedSetButtonsState = false;
	int helpButtonVisibility;
	boolean connectButtonEnabled;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (layout == null || savedInstanceState != null) {
			controllersFragment = new ControllerSwitcherFragment();
			mainActivity = (MainActivity) getActivity();
			preferences = mainActivity.preferences;
			remoteControllerApp = (RemoteControllerApp) mainActivity.getApplication();
			mainViewModel = mainActivity.mainViewModel;
			layout = inflater.inflate(R.layout.connector, container, false);
			tiEditTextIp = layout.findViewById(R.id.editTextIp);
			tiEditTextPort = layout.findViewById(R.id.editTextPort);
			buttonConnect = layout.findViewById(R.id.buttonConnect);
			buttonHelp = layout.findViewById(R.id.buttonHelp);
			connectButtonsParentLayout = layout.findViewById(R.id.connectButtonsParentLayout);

			if (savedInstanceState == null) {
				tiEditTextIp.setText(preferences.getString(MainActivity.KeyPrefer_IP, "192.168.1.3"));
				tiEditTextPort.setText(preferences.getString(MainActivity.KeyPrefer_Port, "5555"));
				buttonHelp.setVisibility(preferences.getBoolean(MainActivity.KeyPrefer_ShowHelpButton, true) ? View.VISIBLE : View.GONE);
			}/* else {
				buttonHelp.setVisibility(mainViewModel.helpButtonVisibility);
			}*/
		}
		if (postDelayedSetButtonsState) {
			postDelayedSetButtonsState = false;
			buttonConnect.setEnabled(connectButtonEnabled);
			buttonHelp.setVisibility(helpButtonVisibility);
		}
		return layout;
	}

	void SavePreferences() {
		int _helpButtonVisibility = postDelayedSetButtonsState ? helpButtonVisibility : buttonHelp.getVisibility();
		preferences.edit()
				.putString(MainActivity.KeyPrefer_IP, tiEditTextIp.getText().toString())
				.putString(MainActivity.KeyPrefer_Port, tiEditTextPort.getText().toString())
				.putBoolean(MainActivity.KeyPrefer_ShowHelpButton, _helpButtonVisibility == View.VISIBLE)
				.apply();
	}

	/*@Override
	public void onPause() {
		Log.e("ConnectorFrag", "onPause()");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.e("ConnectorFrag", "onStop()");
		super.onStop();
	}*/

	@Override
	public void onDestroy() {
		super.onDestroy();
		remoteControllerApp.fetchFullAccessSkuListener = null;
		if (!mainActivity.isChangingConfigurations()) {
			SavePreferences();
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e("ConnectorFrag", "onSaveInstanceState()");
		//mainViewModel.helpButtonVisibility = buttonHelp.getVisibility();
		SavePreferences();
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		Log.e("ConnectorFrag", "onViewStateRestored");
	}

	void ShowHelpButton() {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TransitionManager.beginDelayedTransition(connectButtonsParentLayout, new AutoTransition().setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500).setOrdering(TransitionSet.ORDERING_TOGETHER));
				buttonHelp.setVisibility(View.VISIBLE);
			}
		});
	}

	/*void HideHelpButton() {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mainActivity.runOnUiThread(TransitionManager.beginDelayedTransition(connectButtonsParentLayout, new AutoTransition().setInterpolator(new AccelerateDecelerateInterpolator()));
				buttonHelp.setVisibility(View.GONE);
			}
		});
	}*/

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
				ShowHelpButton();
				mainActivity.OnSocketError(getString(R.string.TimeoutCheckIPportOrUpdate), Toast.LENGTH_LONG);
			} catch (Exception e) {
				ShowHelpButton();
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

			postDelayedSetButtonsState = true;
			connectButtonEnabled = true;
			helpButtonVisibility = View.GONE;
		}
	};
}
