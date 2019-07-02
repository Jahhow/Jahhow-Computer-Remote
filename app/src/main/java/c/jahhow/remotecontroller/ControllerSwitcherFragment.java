package c.jahhow.remotecontroller;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class ControllerSwitcherFragment extends Fragment {
	MainActivity mainActivity;
	Fragment showingController = null;
	BottomNavigationView navigationView;

	ArrowButtonFragment arrowButtonFragment;
	SwipeControllerFragment swipeControllerFragment;
	TouchPadFragment touchPadFragment;
	SendTextFragment sendTextFragment;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.controller_switcher, container, false);
		mainActivity = (MainActivity) getActivity();
		navigationView = view.findViewById(R.id.navBarControllers);
		navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				switch (item.getItemId()) {
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
				return true;
			}
		});
		arrowButtonFragment = new ArrowButtonFragment();
		swipeControllerFragment = new SwipeControllerFragment();
		touchPadFragment = new TouchPadFragment();
		sendTextFragment = new SendTextFragment();

		int preferControllerID = mainActivity.preferences.getInt(MainActivity.KeyPrefer_Controller, R.id.navButtonUseSwiper);
		switch (preferControllerID) {
			case R.id.navButtonUseButtonController:
				showingController = arrowButtonFragment;
				break;
			case R.id.navButtonUseSwiper:
				showingController = swipeControllerFragment;
				break;
			case R.id.navButtonUseTouchPad:
				showingController = touchPadFragment;
				break;
			case R.id.navButtonSendText:
				showingController = sendTextFragment;
				break;
			default:
				mainActivity.preferences.edit().remove(MainActivity.KeyPrefer_Controller).apply();
				preferControllerID = R.id.navButtonUseSwiper;
				showingController = swipeControllerFragment;
		}
		navigationView.setSelectedItemId(preferControllerID);
		getFragmentManager().beginTransaction()
				.add(R.id.ControllerFragmentContainer, showingController).commit();
		return view;
	}

	void ShowFragment(Fragment fragment) {
		if (showingController != fragment) {
			getFragmentManager().beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.remove(showingController)
					.add(R.id.ControllerFragmentContainer, fragment).commit();
			showingController = fragment;
		}
	}

	@Override
	public void onDestroyView() {
		getFragmentManager().beginTransaction().remove(showingController).commit();
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Fragments don't need to place these in onPause()
		mainActivity.preferences.edit().putInt(MainActivity.KeyPrefer_Controller, navigationView.getSelectedItemId()).apply();
		mainActivity.CloseConnection();
	}
}
