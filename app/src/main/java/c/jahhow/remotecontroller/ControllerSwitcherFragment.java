package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.billingclient.api.Purchase.PurchaseState;

public class ControllerSwitcherFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {
	MainActivity mainActivity;
	BottomNavigationView navigationView;
	Fragment showingController = null;

	ArrowButtonFragment arrowButtonFragment = new ArrowButtonFragment();
	SwipeControllerFragment swipeControllerFragment = new SwipeControllerFragment();
	TouchPadFragment touchPadFragment = new TouchPadFragment();
	SendTextFragment sendTextFragment = new SendTextFragment();

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
			if (
					preferControllerID != R.id.navButtonUseButtonController &&
							preferControllerID != R.id.navButtonUseSwiper &&
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
		_onDestroy_onSaveInstanceState();
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
					case R.id.navButtonUseTouchPad:
						touchPadFragment = (TouchPadFragment) showingController;
						break;
					case R.id.navButtonSendText:
						sendTextFragment = (SendTextFragment) showingController;
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
			fragmentTransaction.add(R.id.ControllerFragmentContainer, fragment).commit();
			showingController = fragment;
		}
	}

	void OnPurchaseStateChanged() {
		onNavigationItemSelected(navigationView.getSelectedItemId());
	}

	void _onDestroy_onSaveInstanceState() {
		if (!mainActivity.isChangingConfigurations()) {
			mainActivity.preferences.edit().putInt(MainActivity.KeyPrefer_Controller, navigationView.getSelectedItemId()).apply();
			mainActivity.CloseConnection();
		}
	}

	@Override
	public void onDestroy() {
		showingController = null;
		remoteControllerApp.controllerSwitcherFragment = null;
		_onDestroy_onSaveInstanceState();
		super.onDestroy();
	}

	void onNavigationItemSelected(int id) {
		if (remoteControllerApp.fullAccessState == PurchaseState.PURCHASED) {
			switch (id) {
				case R.id.navButtonUseButtonController:
					ShowFragment(arrowButtonFragment);
					break;
				case R.id.navButtonUseSwiper:
					ShowFragment(swipeControllerFragment);
					break;
				case R.id.navButtonUseTouchPad:
					ShowFragment(touchPadFragment);
					break;
				case R.id.navButtonSendText:
					ShowFragment(sendTextFragment);
					break;
			}
		} else {
			ShowFragment(purchaseFragment);
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		onNavigationItemSelected(item.getItemId());
		return true;
	}
}