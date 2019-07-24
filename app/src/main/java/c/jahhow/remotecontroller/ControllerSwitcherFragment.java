package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.billingclient.api.Purchase.PurchaseState;

public class ControllerSwitcherFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {
	MainActivity mainActivity;

	Fragment showingController = null;
	BottomNavigationView navigationView;

	ArrowButtonFragment arrowButtonFragment = new ArrowButtonFragment();
	SwipeControllerFragment swipeControllerFragment = new SwipeControllerFragment();
	MotionMouseFragment motionMouseFragment = new MotionMouseFragment();
	TouchPadFragment touchPadFragment = new TouchPadFragment();
	InputTextFragment inputTextFragment = new InputTextFragment();

	PurchaseFragment purchaseFragment = new PurchaseFragment();

	RemoteControllerApp remoteControllerApp;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.controller_switcher, container, false);
		mainActivity = (MainActivity) getActivity();
		assert mainActivity != null;
		remoteControllerApp = (RemoteControllerApp) mainActivity.getApplication();
		navigationView = view.findViewById(R.id.navBarControllers);
		navigationView.setOnNavigationItemSelectedListener(this);
		remoteControllerApp.controllerSwitcherFragment = this;
		purchaseFragment.controllerSwitcherFragment = this;

		remoteControllerApp.SyncPurchase();
		if (savedInstanceState == null) {
			int preferControllerID = mainActivity.preferences.getInt(MainActivity.KeyPrefer_Controller, R.id.navButtonUseSwiper);
			if (preferControllerID != R.id.navButtonUseButtonController &&
					preferControllerID != R.id.navButtonUseSwiper &&
					preferControllerID != R.id.navButtonUseMotionMouse &&
					preferControllerID != R.id.navButtonUseTouchPad &&
					preferControllerID != R.id.navButtonSendText) {
				mainActivity.preferences.edit().remove(MainActivity.KeyPrefer_Controller).apply();
				preferControllerID = R.id.navButtonUseSwiper;
			}
			navigationView.setSelectedItemId(preferControllerID);
		}
		return view;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		SavePreference();
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			showingController = getChildFragmentManager().getFragments().get(0);
			if (showingController instanceof PurchaseFragment) {
				purchaseFragment = (PurchaseFragment) showingController;
			} else {
				switch (navigationView.getSelectedItemId()) {
					case R.id.navButtonUseButtonController:
						arrowButtonFragment = (ArrowButtonFragment) showingController;
						break;
					case R.id.navButtonUseSwiper:
						swipeControllerFragment = (SwipeControllerFragment) showingController;
						break;
					case R.id.navButtonUseMotionMouse:
						motionMouseFragment = (MotionMouseFragment) showingController;
						break;
					case R.id.navButtonUseTouchPad:
						touchPadFragment = (TouchPadFragment) showingController;
						break;
					case R.id.navButtonSendText:
						inputTextFragment = (InputTextFragment) showingController;
						break;
				}
			}
		}
	}

	void ShowFragment(Fragment fragment) {
		if (showingController != fragment) {
			FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			if (showingController != null)
				fragmentTransaction.remove(showingController);
			fragmentTransaction.add(R.id.ControllerFragmentContainer, fragment).commitAllowingStateLoss();
			showingController = fragment;
		}
	}

	void OnPurchaseStateChanged() {
		showFragmentById(navigationView.getSelectedItemId());
	}

	void SavePreference() {
		mainActivity.preferences.edit().putInt(MainActivity.KeyPrefer_Controller, navigationView.getSelectedItemId()).apply();
	}

	@Override
	public void onPause() {
		Log.i(getClass().getSimpleName(), "onPause(), isRemoving() == " + isRemoving());
		if (isRemoving()) {
			showingController = null;
			remoteControllerApp.controllerSwitcherFragment = null;
			SavePreference();
			mainActivity.CloseConnection();
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.i(getClass().getSimpleName(), "onStop()");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.i(getClass().getSimpleName(), "onDestroy()");
		super.onDestroy();
	}

	void showFragmentById(int id) {
		if (remoteControllerApp.fullAccessState == PurchaseState.PURCHASED) {
			switch (id) {
				case R.id.navButtonUseButtonController:
					ShowFragment(arrowButtonFragment);
					break;
				case R.id.navButtonUseSwiper:
					ShowFragment(swipeControllerFragment);
					break;
				case R.id.navButtonUseMotionMouse:
					ShowFragment(motionMouseFragment);
					break;
				case R.id.navButtonUseTouchPad:
					ShowFragment(touchPadFragment);
					break;
				case R.id.navButtonSendText:
					ShowFragment(inputTextFragment);
					break;
			}
		} else {
			ShowFragment(purchaseFragment);
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		showFragmentById(item.getItemId());
		return true;
	}
}