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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static c.jahhow.remotecontroller.MainActivity.preferences;

public class ConnectorSwitcherFragment extends MyFragment implements BottomNavigationView.OnNavigationItemSelectedListener {
    static final boolean
            PreferBluetooth = true,
            PreferTcpIp = false;

    MainActivity mainActivity;
    private BottomNavigationView navBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.connector_switcher, container, false);
        navBar = layout.findViewById(R.id.navBarConnectors);
        navBar.setOnNavigationItemSelectedListener(this);
        if (hasNoChildFragment()) {
            boolean preferredConnector = preferences.getBoolean(MainActivity.KeyPrefer_Connector, PreferTcpIp);
            navBar.setSelectedItemId(preferredConnector == PreferBluetooth ?
                    R.id.navButtonBluetooth : R.id.navButtonInternet);
        }
        //Log.i(getClass().getSimpleName(), String.format("isRestoringState() => %b", isRestoringState()));
        //Log.i(getClass().getSimpleName(), String.format("savedInstanceState %c= null", savedInstanceState == null ? '=' : '!'));
        return layout;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (showingFragmentID == 0) {
            showingFragmentID = navBar.getSelectedItemId();
            //Log.i(this.getClass().getSimpleName(), "onViewStateRestored() showingFragmentID = " + showingFragmentID);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!mainActivity.isChangingConfigurations())
            mainActivity.stopAutoTcpConnect();
        preferences.edit().putBoolean(MainActivity.KeyPrefer_Connector, showingFragmentID == R.id.navButtonBluetooth ? PreferBluetooth : PreferTcpIp).apply();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (showingFragmentID != 0)
            preferences.edit().putBoolean(MainActivity.KeyPrefer_Connector, showingFragmentID == R.id.navButtonBluetooth ? PreferBluetooth : PreferTcpIp).apply();
    }

    private int showingFragmentID = 0;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id != showingFragmentID) {
            showingFragmentID = id;
            Fragment fragmentToShow = null;
            if (id == R.id.navButtonBluetooth) {
                fragmentToShow = new BluetoothConnectorFragment();
                mainActivity.stopAutoTcpConnect();
            } else {
                fragmentToShow = new TcpIpConnectorFragment();
                mainActivity.startAutoTcpConnect();
            }
            //Log.i(this.getClass().getSimpleName(), "Manually Added Fragment");
            getChildFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.ConnectorFragmentContainer, fragmentToShow).commit();
        }
        return true;
    }
}
