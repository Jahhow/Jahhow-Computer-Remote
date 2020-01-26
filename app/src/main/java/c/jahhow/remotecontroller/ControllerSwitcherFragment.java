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

public class ControllerSwitcherFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {
    MainActivity mainActivity;
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

    private void ShowFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.ControllerFragmentContainer, fragment).commitAllowingStateLoss();
    }

    void OnPurchaseStateChanged() {
        showFragmentById(navigationView.getSelectedItemId());
    }

    private void SavePreference() {
        mainActivity.preferences.edit().putInt(MainActivity.KeyPrefer_Controller, navigationView.getSelectedItemId()).apply();
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
        if (!BuildConfig.DEBUG && remoteControllerApp.fullAccessState != PurchaseState.PURCHASED) {
            id = R.id.purchaseFragment;
        }
        if (id != showingFragmentID) {
            showingFragmentID = id;
            switch (id) {
                case R.id.navButtonUseButtonController:
                    ShowFragment(new ArrowButtonFragment());
                    break;
                case R.id.navButtonUseSwiper:
                    ShowFragment(new SwipeControllerFragment());
                    break;
                case R.id.navButtonUseMotionMouse:
                    ShowFragment(new MotionMouseFragment());
                    break;
                case R.id.navButtonUseTouchPad:
                    ShowFragment(new TouchPadFragment());
                    break;
                case R.id.navButtonSendText:
                    ShowFragment(new InputTextFragment());
                    break;
                case R.id.purchaseFragment:
                    ShowFragment(new PurchaseFragment(this));
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
}