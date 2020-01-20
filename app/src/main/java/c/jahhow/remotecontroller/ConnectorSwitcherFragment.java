package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ConnectorSwitcherFragment extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    private Fragment showingFragment = null;
    private ConnectorFragment connectorFragment = new ConnectorFragment();
    private BluetoothConnectorFragment bluetoothConnectorFragment = new BluetoothConnectorFragment();

    private BottomNavigationView navBar;

    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(this.getClass().getSimpleName(), String.format("onCreate (savedInstanceState == null) -> %b", savedInstanceState == null));
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*if (savedInstanceState == null) {
            connectorFragment = new ConnectorFragment();
            bluetoothConnectorFragment = new BluetoothConnectorFragment();
        }*/
        View layout = inflater.inflate(R.layout.fragment_connector_switcher, container, false);
        navBar = layout.findViewById(R.id.navBarConnectors);
        navBar.setOnNavigationItemSelectedListener(this);
        navBar.setSelectedItemId(navBar.getSelectedItemId());
        return layout;
    }

    /*@Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(this.getClass().getSimpleName(), String.format("onSaveInstanceState (outState == null) -> %b", outState == null));
    }*/

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.i(this.getClass().getSimpleName(), String.format("onViewStateRestored (savedInstanceState == null) -> %b", savedInstanceState == null));
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            showingFragment = getChildFragmentManager().getFragments().get(0);
            switch (navBar.getSelectedItemId()) {
                case R.id.navButtonInternet:
                    connectorFragment = (ConnectorFragment) showingFragment;
                    break;
                case R.id.navButtonBluetooth:
                    bluetoothConnectorFragment = (BluetoothConnectorFragment) showingFragment;
                    break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragmentToShow = null;
        switch (menuItem.getItemId()) {
            case R.id.navButtonInternet:
                fragmentToShow = connectorFragment;
                break;
            case R.id.navButtonBluetooth:
                fragmentToShow = bluetoothConnectorFragment;
                break;
        }
        if (showingFragment != fragmentToShow) {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            if (showingFragment != null)
                fragmentTransaction.remove(showingFragment);
            assert fragmentToShow != null;
            fragmentTransaction.add(R.id.ConnectorFragmentContainer, fragmentToShow).commitAllowingStateLoss();
            showingFragment = fragmentToShow;
        }
        return true;
    }
}
