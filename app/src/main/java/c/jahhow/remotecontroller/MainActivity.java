package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import c.jahhow.remotecontroller.Msg.ButtonAction;
import c.jahhow.remotecontroller.Msg.Msg;
import c.jahhow.remotecontroller.Msg.SCS1;

public class MainActivity extends AppCompatActivity {
	MainViewModel mainViewModel;
	private BillingClient billingClient;

	ConnectorFragment connectorFragment;
	Fragment showingFragment;
	SharedPreferences preferences;
	static final String name_CommonSharedPrefer = "CommonSettings";
	static final String KeyPrefer_IP = "0";
	static final String KeyPrefer_Port = "1";
	static final String KeyPrefer_Controller = "2";
	static final String KeyPrefer_SwipeDemo = "3";
	static final String KeyPrefer_Swiped = "4";
	static final String KeyPrefer_InputText = "5";
	static final String KeyPrefer_VibrateOnDownOnly = "6";

	Toast toast;

	@SuppressLint({"ShowToast", "InflateParams"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
		connectorFragment = new ConnectorFragment();
		getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.add(android.R.id.content, connectorFragment).commit();
		showingFragment = connectorFragment;
		toast = Toast.makeText(this, null, Toast.LENGTH_SHORT);
		preferences = getSharedPreferences(name_CommonSharedPrefer, 0);
/*
		billingClient = BillingClient.newBuilder(this).setListener(new PurchasesUpdatedListener() {
			@Override
			public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
				if (billingResult.getResponseCode() == BillingResponseCode.OK) {
					for (Purchase purchase : purchases) {
						if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
						}
					}
				}
			}
		}).build();
		billingClient.startConnection(new BillingClientStateListener() {
			@Override
			public void onBillingSetupFinished(BillingResult billingResult) {
				if (billingResult.getResponseCode() == BillingResponseCode.OK) {
					// The BillingClient is ready. You can query purchases here.
					List<String> skuList = new ArrayList<>();
					skuList.add("premium_upgrade");
					skuList.add("gas");
					SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
					params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
					billingClient.querySkuDetailsAsync(params.build(),
							new SkuDetailsResponseListener() {
								@Override
								public void onSkuDetailsResponse(BillingResult billingResult,
																 List<SkuDetails> skuDetailsList) {
									// Process the result.
									if (billingResult.getResponseCode() == BillingResponseCode.OK) {
										for (SkuDetails skuDetails : skuDetailsList) {
											String sku = skuDetails.getSku();
											String price = skuDetails.getPrice();
											if ("premium_upgrade".equals(sku)) {
											} else if ("gas".equals(sku)) {
											}
										}
									}
								}
							});
				}
			}

			@Override
			public void onBillingServiceDisconnected() {
				// Try to restart the connection on the next request to
				// Google Play by calling the startConnection() method.
			}
		});*/
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
					OnSocketError(getString(R.string.Disconnected));
					e.printStackTrace();
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
					OnSocketError(getString(R.string.Disconnected));
					e.printStackTrace();
				}
			}
		});
	}

	public void SendInputText(final String text, byte mode, boolean Hold) {
		if (mainViewModel.socketHandler == null || text.length() == 0)
			return;

		try {
			int textByteLen = text.length() << 1;
			final byte packet[] = ByteBuffer.allocate(7 + textByteLen)
					.put(Msg.PasteText)
					.putInt(textByteLen)
					.put(mode)
					.put((byte) (Hold ? 1 : 0))
					.put(text.getBytes("UTF-16LE")).array();

			mainViewModel.socketHandler.post(new Runnable() {
				@Override
				public void run() {
					try {
						mainViewModel.socketOutput.write(packet);
					} catch (IOException e) {
						OnSocketError(getString(R.string.Disconnected));
						e.printStackTrace();
					}
				}
			});
		} catch (UnsupportedEncodingException e) {
			ShowToast(getString(R.string.ProblemSendingText));
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
						OnSocketError(getString(R.string.Disconnected));
						e.printStackTrace();
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
					OnSocketError(getString(R.string.Disconnected));
					e.printStackTrace();
				}
			}
		});
	}
/*
	public void SendKeyboardUp(byte VirtualKeyCode) {
		final byte[] bytes = {Msg.KeyboardUp, VirtualKeyCode};
		if(mainViewModel.socketHandler!=null)mainViewModel.socketHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnSocketError("Problem sending a key");
					e.printStackTrace();
				}
			}
		});
	}

	public void SendKeyboardScanCode(byte VirtualKeyCode) {
		final byte[] bytes = {Msg.KeyboardClick, VirtualKeyCode};
		if(mainViewModel.socketHandler!=null)mainViewModel.socketHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnSocketError("Problem sending a key");
					e.printStackTrace();
				}
			}
		});
	}*/

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

		final byte packet[] = byteBuffer.array();
		mainViewModel.socketHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(packet);
				} catch (IOException e) {
					OnSocketError(getString(R.string.Disconnected));
					e.printStackTrace();
				}
			}
		});
	}

	void ShowToast(String text, int duration) {
		toast.setText(text);
		toast.setDuration(duration);
		toast.show();
	}

	void ShowToast(String text) {
		ShowToast(text, Toast.LENGTH_SHORT);
	}

	void OnSocketError(final String showToast, final int toastDuration) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				connectorFragment.buttonConnect.setEnabled(true);
				CloseConnection();
				getFragmentManager().popBackStack();
				ShowToast(showToast, toastDuration);
			}
		});
	}

	void OnSocketError(final String showToast) {
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