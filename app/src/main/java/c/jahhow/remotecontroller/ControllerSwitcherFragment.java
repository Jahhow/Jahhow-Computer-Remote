package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.billingclient.api.Purchase.PurchaseState;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static c.jahhow.remotecontroller.MainActivity.preferences;

public class ControllerSwitcherFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {
    private MainActivity mainActivity;
    private BottomNavigationView navigationView;
    private RemoteControllerApp remoteControllerApp;

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

        if (savedInstanceState == null) {
            int preferControllerID = preferences.getInt(MainActivity.KeyPrefer_Controller, R.id.navButtonUseAirMouse);
            if (preferControllerID != R.id.navButtonUseButtonController &&
                    preferControllerID != R.id.navButtonUseSwiper &&
                    preferControllerID != R.id.navButtonUseAirMouse &&
                    preferControllerID != R.id.navButtonUseTouchPad &&
                    preferControllerID != R.id.navButtonSendText) {
                preferences.edit().remove(MainActivity.KeyPrefer_Controller).apply();
                preferControllerID = R.id.navButtonUseSwiper;
            }
            navigationView.setSelectedItemId(preferControllerID);
        }
        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (showingFragmentID == 0) {
            if (shouldShowPurchaseFragment())
                showingFragmentID = R.id.purchaseFragment;
            else
                showingFragmentID = navigationView.getSelectedItemId();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        SavePreference();
    }

    private void ShowFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.ControllerFragmentContainer, fragment).commitAllowingStateLoss();
    }

    void OnPurchaseStateChanged() {
        showFragmentById(navigationView.getSelectedItemId());
    }

    private void SavePreference() {
        preferences.edit().putInt(MainActivity.KeyPrefer_Controller, navigationView.getSelectedItemId()).apply();
    }

    @Override
    public void onPause() {
        if (isRemoving()) {
            remoteControllerApp.controllerSwitcherFragment = null;
            SavePreference();
            mainActivity.CloseConnection();
        }
        super.onPause();
    }

    private void showFragmentById(int id) {
        if (shouldShowPurchaseFragment()) {
            id = R.id.purchaseFragment;
        }
        if (id != showingFragmentID) {
            //Log.i(getClass().getSimpleName(), "Manually Adding Fragment");
            showingFragmentID = id;
            switch (id) {
                case R.id.navButtonUseButtonController:
                    ShowFragment(new ArrowButtonFragment());
                    break;
                case R.id.navButtonUseSwiper:
                    ShowFragment(new SwipeControllerFragment());
                    break;
                case R.id.navButtonUseAirMouse:
                    ShowFragment(new AirMouseFragment());
                    break;
                case R.id.navButtonUseTouchPad:
                    ShowFragment(new TouchPadFragment());
                    break;
                case R.id.navButtonSendText:
                    ShowFragment(new InputTextFragment());
                    break;
                case R.id.purchaseFragment:
                    ShowFragment(new PurchaseFragment());
                    break;
            }
        }
    }

    private int showingFragmentID = 0;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        showFragmentById(item.getItemId());
        return true;
    }

    private boolean shouldShowPurchaseFragment() {
        if (remoteControllerApp.purchaseSkipped || remoteControllerApp.purchaseState == PurchaseState.PURCHASED)
            return false;
        if (preferences.getInt(MainActivity.KeyPrefer_SuccessfulConnectionCount, 0) > 10) {
            preferences.edit().putInt(MainActivity.KeyPrefer_SuccessfulConnectionCount, 0).apply();
            return true;
        }
        return false;
    }
}