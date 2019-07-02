package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import c.jahhow.remotecontroller.Msg.ButtonAction;
import c.jahhow.remotecontroller.Msg.Msg;
import c.jahhow.remotecontroller.Msg.SCS1;

public class MainActivity extends AppCompatActivity {
	MainViewModel mainViewModel;

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
	}

	public void ButtonClick_Connect(View v) {
		v.setEnabled(false);

		/*InputMethodManager inputMethodManager =
				(InputMethodManager) getSystemService(
						Activity.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null)
			inputMethodManager.hideSoftInputFromWindow(
					v.getWindowToken(), 0);*/

		new Thread(connectorFragment.connectRunnable).start();
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
		final byte[] bytes = ByteBuffer.allocate(5).put(Msg.MoveMouse).putShort(dx).putShort(dy).array();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnProblemSending("Problem Moving Mouse");
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void SendMouseWheel(int amount) {
		final byte[] bytes = ByteBuffer.allocate(5).put(Msg.MouseWheel).putInt(amount).array();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnProblemSending("Problem Sending a Message");
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void SendPasteText(final String text, boolean Hold) {
		try {
			int textByteLen = text.length() << 1;
			final byte packet[] = ByteBuffer.allocate(6 + textByteLen)
					.put(Msg.PasteText)
					.putInt(textByteLen)
					.put((byte) (Hold ? 1 : 0))
					.put(text.getBytes("UTF-16LE")).array();

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						mainViewModel.socketOutput.write(packet);
					} catch (IOException e) {
						OnProblemSending("Problem Sending Text");
						e.printStackTrace();
					}
				}
			}).start();
		} catch (UnsupportedEncodingException e) {
			ShowToast("Problem Sending Text");
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(msg);
				} catch (IOException e) {
					OnProblemSending("Problem Sending a Message");
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void SendKeyboardScanCode(short ScanCode, byte buttonAction) {
		final byte[] bytes = ByteBuffer.allocate(4)
				.put(Msg.KeyboardScanCode)
				.putShort(ScanCode)
				.put(buttonAction)
				.array();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnProblemSending("Problem sending a key");
					e.printStackTrace();
				}
			}
		}).start();
	}
/*
	public void SendKeyboardUp(byte VirtualKeyCode) {
		final byte[] bytes = {Msg.KeyboardUp, VirtualKeyCode};
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnProblemSending("Problem sending a key");
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void SendKeyboardScanCode(byte VirtualKeyCode) {
		final byte[] bytes = {Msg.KeyboardClick, VirtualKeyCode};
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(bytes);
				} catch (IOException e) {
					OnProblemSending("Problem sending a key");
					e.printStackTrace();
				}
			}
		}).start();
	}*/


	// actionType: ButtonAction.{Click, Down, or Up}
	public void SendKeyboardScanCodeCombination(final byte actionType, final short... ScanCodes) {
		int scanCodeByteLen = ScanCodes.length << 1;
		ByteBuffer byteBuffer = ByteBuffer.allocate(3 + scanCodeByteLen)
				.put(Msg.KeyboardScanCodeCombination)
				.put(actionType)
				.put((byte) scanCodeByteLen);
		for (short scanCode : ScanCodes)
			byteBuffer.putShort(scanCode);

		final byte packet[] = byteBuffer.array();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mainViewModel.socketOutput.write(packet);
				} catch (IOException e) {
					OnProblemSending("Problem sending keys");
					e.printStackTrace();
				}
			}
		}).start();
	}

	void ShowToast(String text) {
		toast.setText(text);
		toast.show();
	}

	void OnProblemSending(final String showToast) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				CloseConnection();
				getFragmentManager().popBackStack();
				ShowToast(showToast);
			}
		});
	}

	// Please Call it on UI Thread
	void CloseConnection() {
		if (mainViewModel.socket != null) {
			try {
				mainViewModel.socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mainViewModel.socket = null;
		}
	}
}