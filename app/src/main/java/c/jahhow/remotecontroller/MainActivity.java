package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.app.Service;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase.PurchaseState;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import c.jahhow.remotecontroller.msg.ButtonAction;
import c.jahhow.remotecontroller.msg.Msg;
import c.jahhow.remotecontroller.msg.SCS1;

public class MainActivity extends AppCompatActivity {
	RemoteControllerApp remoteControllerApp;
	MainViewModel mainViewModel;
	ConnectorFragment connectorFragment;

	SharedPreferences preferences;
	static final String name_CommonSharedPrefer = "CommonSettings",
			KeyPrefer_IP = "0",
			KeyPrefer_Port = "1",
			KeyPrefer_Controller = "2",
			KeyPrefer_SwipeDemo = "3",
			KeyPrefer_Swiped = "4",
			KeyPrefer_InputText = "5",
			KeyPrefer_VibrateOnDown = "6",
			KeyPrefer_ShowHelpButton = "7";

	Toast toast;
	Vibrator vibrator;

	public void OnClick_ManagePlaySubs(MenuItem v) {
		if (remoteControllerApp.fullAccessState == PurchaseState.UNSPECIFIED_STATE) {
			// launchBillingFlow
			if (remoteControllerApp.skuDetailsFullAccess == null) {
				remoteControllerApp.fetchFullAccessSkuListener = new RemoteControllerApp.FetchFullAccessSkuListener() {
					@Override
					public void onSkuDetailsReady() {
						if (remoteControllerApp.skuDetailsFullAccess == null) {
							Toast.makeText(getApplicationContext(), R.string.FailedToReachGooglePlay, Toast.LENGTH_SHORT).show();
						} else {
							remoteControllerApp.billingClient.launchBillingFlow(MainActivity.this, BillingFlowParams.newBuilder().setSkuDetails(remoteControllerApp.skuDetailsFullAccess).build());
						}
					}
				};
				remoteControllerApp.SyncPurchase();
				if (remoteControllerApp.fullAccessState != PurchaseState.UNSPECIFIED_STATE) {
					remoteControllerApp.OpenPlayStoreManageSubscription();
				}
			} else {
				remoteControllerApp.billingClient.launchBillingFlow(this, BillingFlowParams.newBuilder().setSkuDetails(remoteControllerApp.skuDetailsFullAccess).build());
			}
		} else {
			remoteControllerApp.OpenPlayStoreManageSubscription();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	static final String FragmentTag_Connector = "0";

	@SuppressLint({"ShowToast", "InflateParams"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
		if (vibrator != null && !vibrator.hasVibrator()) vibrator = null;
		preferences = getSharedPreferences(name_CommonSharedPrefer, 0);
		remoteControllerApp = (RemoteControllerApp) getApplication();
		toast = Toast.makeText(this, null, Toast.LENGTH_SHORT);
		mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
		if (savedInstanceState == null) {
			//remoteControllerApp.StartBillingClient();
			connectorFragment = new ConnectorFragment();
			getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.add(android.R.id.content, connectorFragment, FragmentTag_Connector).commit();
		}
	}

	@Override
	protected void onDestroy() {
		//Log.e(getLocalClassName(), "onDestroy() isFinishing() == " + isFinishing());
		/*if (isFinishing()) {
			remoteControllerApp.EndBillingClient();
		}*/
		remoteControllerApp.fetchFullAccessSkuListener = null;
		super.onDestroy();
	}

	static final String JahhowAppWebsite = "http://jahhowapp.blogspot.com/2019/07/computer-remote-controller.html";

	public void OpenJahhowAppWebsite(View v) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(JahhowAppWebsite)));
	}

	public void buttonShowHelpFragment(View v) {
		getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.addToBackStack(null)
				.replace(android.R.id.content, new HelpFragment()).commit();
	}

	public void OpenBuildConnection(View v) {
		getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.addToBackStack(null)
				.replace(android.R.id.content, new ConnectionGuideFragment()).commit();
	}

	public void PopFragmentStack(View v) {
		getSupportFragmentManager().popBackStack();
	}

	void Vibrate(long ms) {
		if (ms > 0 && vibrator != null)
			vibrator.vibrate(ms);
	}

	public void ButtonClick_Connect(View v) {
		v.setEnabled(false);
		mainViewModel.socketHandlerThread = new HandlerThread("");
		mainViewModel.socketHandlerThread.start();
		mainViewModel.socketHandler = new Handler(mainViewModel.socketHandlerThread.getLooper());
		mainViewModel.socketHandler.post(connectorFragment.connectRunnable);
	}

	public void SendClick_Left(View v) {
		SendKeyboardScanCode(SCS1.Left_Arrow, ButtonAction.Click);
	}

	public void SendClick_Up(View v) {
		SendKeyboardScanCode(SCS1.Up_Arrow, ButtonAction.Click);
	}

	public void SendClick_Right(View v) {
		SendKeyboardScanCode(SCS1.Right_Arrow, ButtonAction.Click);
	}

	public void SendClick_Down(View v) {
		SendKeyboardScanCode(SCS1.Dn_Arrow, ButtonAction.Click);
	}

	public void SendClick_Esc(View v) {
		SendKeyboardScanCode(SCS1.Esc, ButtonAction.Click);
	}

	public void SendClick_Home(View v) {
		SendKeyboardScanCode(SCS1.Home, ButtonAction.Click);
	}

	public void SendClick_End(View v) {
		SendKeyboardScanCode(SCS1.End, ButtonAction.Click);
	}

	public void SendClick_F5(View v) {
		SendKeyboardScanCode(SCS1.F5, ButtonAction.Click);
	}

	public void SendClick_Backspace(View v) {
		SendKeyboardScanCode(SCS1.Backspace, ButtonAction.Click);
	}

	public void SendClick_Enter(View v) {
		SendKeyboardScanCode(SCS1.Enter, ButtonAction.Click);
	}

	public void SendClick_ShiftF5(View v) {
		SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_SHIFT, SCS1.F5);
	}

	public void SendClick_CtrlC(View v) {
		SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_CTRL, SCS1.C);
	}

	public void SendClick_CtrlV(View v) {
		SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_CTRL, SCS1.V);
	}

	public void SendClick_CtrlA(View v) {
		SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_CTRL, SCS1.A);
	}

	public void SendClick_CtrlX(View v) {
		SendKeyboardScanCodeCombination(ButtonAction.Click, SCS1.L_CTRL, SCS1.X);
	}

	public void SendMouseMove(short dx, short dy) {
		if (mainViewModel.socketHandler == null)
			return;

		final byte[] bytes = ByteBuffer.allocate(5).put(Msg.MoveMouse).putShort(dx).putShort(dy).array();
		mainViewModel.socketHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnSocketError(R.string.Disconnected);
				}
			}
		});
	}

	public void SendMouseWheel(int amount) {
		final byte[] bytes = ByteBuffer.allocate(5).put(Msg.MouseWheel).putInt(amount).array();
		mainViewModel.socketHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnSocketError(R.string.Disconnected);
				}
			}
		});
	}

	static final String SendTextEncode = "UTF-16LE";

	public void SendInputText(final String text, byte mode, boolean Hold) {
		if (mainViewModel.socketHandler == null || text.length() == 0)
			return;

		try {
			int textByteLen = text.length() << 1;
			final byte[] packet = ByteBuffer.allocate(7 + textByteLen)
					.put(Msg.PasteText)
					.putInt(textByteLen)
					.put(mode)
					.put((byte) (Hold ? 1 : 0))
					.put(text.getBytes(SendTextEncode)).array();

			mainViewModel.socketHandler.post(new Runnable() {
				@Override
				public void run() {
					try {
						mainViewModel.socketOutput.write(packet);
					} catch (IOException e) {
						OnSocketError(R.string.Disconnected);
					}
				}
			});
		} catch (UnsupportedEncodingException e) {
			ShowToast(R.string.ProblemSendingText);
			e.printStackTrace();
		}
	}

	public void SendMouseLeftDown() {
		SendMsg(Msg.MouseLeftDown);
	}

	public void SendMouseLeftUp() {
		SendMsg(Msg.MouseLeftUp);
	}

	public void SendMouseLeftClick() {
		SendMsg(Msg.MouseLeftClick);
	}

	public void SendMouseRightDown() {
		SendMsg(Msg.MouseRightDown);
	}

	public void SendMouseRightUp() {
		SendMsg(Msg.MouseRightUp);
	}

	public void SendMouseRightClick() {
		SendMsg(Msg.MouseRightClick);
	}

	public void SendMsg(final byte msg) {
		if (mainViewModel.socketHandler != null)
			mainViewModel.socketHandler.post(new Runnable() {
				@Override
				public void run() {
					try {
						mainViewModel.socketOutput.write(msg);
					} catch (IOException e) {
						OnSocketError(R.string.Disconnected);
					}
				}
			});
	}

	public void SendKeyboardScanCode(short ScanCode, byte buttonAction) {
		if (mainViewModel.socketHandler == null)
			return;

		final byte[] bytes = ByteBuffer.allocate(4)
				.put(Msg.KeyboardScanCode)
				.putShort(ScanCode)
				.put(buttonAction)
				.array();
		mainViewModel.socketHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnSocketError(R.string.Disconnected);
				}
			}
		});
	}

	// actionType: ButtonAction.{Click, Down, or Up}
	public void SendKeyboardScanCodeCombination(final byte actionType, final short... ScanCodes) {
		if (mainViewModel.socketHandler == null)
			return;

		int scanCodeByteLen = ScanCodes.length << 1;
		ByteBuffer byteBuffer = ByteBuffer.allocate(3 + scanCodeByteLen)
				.put(Msg.KeyboardScanCodeCombination)
				.put(actionType)
				.put((byte) scanCodeByteLen);
		for (short scanCode : ScanCodes)
			byteBuffer.putShort(scanCode);

		final byte[] packet = byteBuffer.array();
		mainViewModel.socketHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(packet);
				} catch (IOException e) {
					OnSocketError(R.string.Disconnected);
				}
			}
		});
	}

	void ShowToast(@StringRes int resId) {
		ShowToast(resId, Toast.LENGTH_SHORT);
	}

	void ShowToast(@StringRes int resId, int duration) {
		ShowToast(getString(resId), Toast.LENGTH_SHORT);
	}

	void ShowToast(String text, int duration) {
		toast.setText(text);
		toast.setDuration(duration);
		toast.show();
	}

	/*@Override
	protected void onPause() {
		Log.e(getLocalClassName(), "onPause() isFinishing() == " + isFinishing());
		super.onPause();
	}*/

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		connectorFragment = (ConnectorFragment) getSupportFragmentManager().findFragmentByTag(FragmentTag_Connector);
		super.onRestoreInstanceState(savedInstanceState);
	}

	void OnSocketError(@StringRes final int showToast, final int toastDuration) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!getSupportFragmentManager().isStateSaved()) {
					getSupportFragmentManager().popBackStack();
				}
				if (!isFinishing()) {
					connectorFragment.buttonConnect.setEnabled(true);
					ShowToast(showToast, toastDuration);
				}
				CloseConnection();
			}
		});
	}

	void OnSocketError(@StringRes int showToast) {
		OnSocketError(showToast, Toast.LENGTH_SHORT);
	}

	// Please Call it on UI Thread
	void CloseConnection() {
		if (mainViewModel.socket != null) {
			mainViewModel.socketHandlerThread.quit();
			mainViewModel.socketHandlerThread = null;
			mainViewModel.socketHandler = null;//todo
			try {
				mainViewModel.socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mainViewModel.socket = null;
		}
	}
}