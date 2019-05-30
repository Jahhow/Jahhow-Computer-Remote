package c.jahhow.remotecontroller;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MainActivity extends AppCompatActivity {
	Socket socket = null;
	OutputStream socketOutput = null;

	TextInputEditText tiEditTextIp, tiEditTextPort;
	Button buttonConnect;
	LinearLayout controlPanel;
	Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tiEditTextIp = findViewById(R.id.editTextIp);
		tiEditTextPort = findViewById(R.id.editTextPort);
		buttonConnect = findViewById(R.id.buttonConnect);
		controlPanel = findViewById(R.id.controlPanel);
		toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
	}

	private Runnable Connect = new Runnable() {
		@Override
		public void run() {
			try {
				InetSocketAddress inetaddr = new InetSocketAddress(
						tiEditTextIp.getText().toString(),
						Integer.parseInt(tiEditTextPort.getText().toString())
				);
				socket = new Socket();
				socket.connect(inetaddr, 5000);
				socket.shutdownInput();
				socketOutput = socket.getOutputStream();
				controlPanel.post(new Runnable() {
					@Override
					public void run() {
						controlPanel.setVisibility(View.VISIBLE);
						AlphaAnimation a = new AlphaAnimation(0, 1);
						a.setDuration(250);
						controlPanel.startAnimation(a);
					}
				});
			} catch (SocketTimeoutException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						toast.setText("Timeout");
						toast.show();
						CloseConnection();
					}
				});
			} catch (Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						toast.setText("Connection Error");
						toast.show();
						CloseConnection();
					}
				});
				Log.e("MainActivity", "Error Creating Socket" + e.toString());
				e.printStackTrace();
			}
		}
	};
	/*
	View.OnTouchListener key_OnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

			} else {

			}
			return true;
		}
	}*/

	@Override
	protected void onPause() {
		CloseConnection();
		super.onPause();
	}

	public void ButtonClick_Connect(View v) {
		v.setEnabled(false);

		InputMethodManager inputMethodManager =
				(InputMethodManager) getSystemService(
						Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(
				getCurrentFocus().getWindowToken(), 0);

		new Thread(Connect).start();
	}

	static final byte KeyEvent_KeyPress = 0;
	static final byte KeyEvent_KeyRelease = 1;
	static final byte KeyEvent_KeyClick = 2; // Press + Release

	public void ButtonClick_Left(View v) {
		SendKeyClick((byte) 0x25);
	}

	public void ButtonClick_Up(View v) {
		SendKeyClick((byte) 0x26);
	}

	public void ButtonClick_Right(View v) {
		SendKeyClick((byte) 0x27);
	}

	public void ButtonClick_Down(View v) {
		SendKeyClick((byte) 0x28);
	}

	public void SendKeyClick(byte VirtualKeyCode) {
		final byte[] bytes = {VirtualKeyCode};
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					socketOutput.write(bytes);
					//socketOutput.flush();
				} catch (IOException e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							CloseConnection();
							toast.setText("Problem sending a key");
							toast.show();
						}
					});
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	// Please Call it on UI Thread
	public void onBackPressed() {
		if (controlPanel.getVisibility() == View.VISIBLE) {
			CloseConnection();
		} else super.onBackPressed();
	}

	// Please Call it on UI Thread
	void CloseConnection() {
		controlPanel.setVisibility(View.GONE);
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			socket = null;
			socketOutput = null;
		}
		buttonConnect.setEnabled(true);
	}
}