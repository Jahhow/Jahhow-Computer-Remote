package c.jahhow.remotecontroller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MainActivity extends AppCompatActivity {
	Socket socket = null;
	OutputStream socketOutput = null;

	FrameLayout contentView;
	View ipLayout, controlPanel;
	Animation fadeIn;
	TextInputEditText tiEditTextIp, tiEditTextPort;
	Button buttonConnect;
	Toast toast;

	static final int idControlPanel = 2;

	SharedPreferences preferences;
	static final String name_CommonSharedPrefer = "CommonSettings";
	static final String KeyPrefer_IP = "IP";
	static final String KeyPrefer_Port = "Port";

	@SuppressLint({"ShowToast", "InflateParams"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		contentView = findViewById(android.R.id.content);
		ipLayout = getLayoutInflater().inflate(R.layout.activity_main, null);
		setContentView(ipLayout);
		controlPanel = getLayoutInflater().inflate(R.layout.control_panel, null);
		controlPanel.setId(idControlPanel);
		tiEditTextIp = findViewById(R.id.editTextIp);
		tiEditTextPort = findViewById(R.id.editTextPort);
		buttonConnect = findViewById(R.id.buttonConnect);
		toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);

		preferences = getSharedPreferences(name_CommonSharedPrefer, 0);
		tiEditTextIp.setText(preferences.getString(KeyPrefer_IP, ""));
		tiEditTextPort.setText(preferences.getString(KeyPrefer_Port, ""));
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
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setContentView(controlPanel);
						controlPanel.startAnimation(fadeIn);
					}
				});
			} catch (SocketTimeoutException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						toast.setText("Timeout");
						toast.show();
					}
				});
				socket = null;
			} catch (Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						toast.setText("Connection Error");
						toast.show();
						CloseConnection();
					}
				});
				socket = null;
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

		// id of the view passed in setContentView
		int viewId = contentView.getChildAt(0).getId();

		if (viewId == idControlPanel) {
			CloseConnection();
		}
		preferences.edit()
				.putString(KeyPrefer_IP, tiEditTextIp.getText().toString())
				.putString(KeyPrefer_Port, tiEditTextPort.getText().toString())
				.apply();
		super.onPause();
	}

	public void ButtonClick_Connect(View v) {
		v.setEnabled(false);

		InputMethodManager inputMethodManager =
				(InputMethodManager) getSystemService(
						Activity.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null)
			inputMethodManager.hideSoftInputFromWindow(
					v.getWindowToken(), 0);

		new Thread(Connect).start();
	}

	/*
	static final byte KeyEvent_KeyPress = 0;
	static final byte KeyEvent_KeyRelease = 1;
	static final byte KeyEvent_KeyClick = 2; // Press + Release
	*/

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

		// id of the view passed in setContentView
		int viewId = contentView.getChildAt(0).getId();

		if (viewId == idControlPanel) {
			CloseConnection();
		} else super.onBackPressed();
	}

	// Please Call it on UI Thread
	void CloseConnection() {
		setContentView(ipLayout);
		ipLayout.startAnimation(fadeIn);
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			socket = null;
		}
		buttonConnect.setEnabled(true);
	}
}