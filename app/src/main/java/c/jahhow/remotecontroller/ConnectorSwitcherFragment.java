package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ConnectorSwitcherFragment extends MyFragment implements BottomNavigationView.OnNavigationItemSelectedListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.connector_switcher, container, false);
        BottomNavigationView navBar = layout.findViewById(R.id.navBarConnectors);
        navBar.setOnNavigationItemSelectedListener(this);
        if (!isRestoringState()) {
            navBar.setSelectedItemId(R.id.navButtonBluetooth/*navBar.getSelectedItemId()*/);
        }
        //Log.i(getClass().getSimpleName(), String.format("isRestoringState() => %b", isRestoringState()));
        //Log.i(getClass().getSimpleName(), String.format("savedInstanceState %c= null", savedInstanceState == null ? '=' : '!'));
        return layout;
    }

    private int showingFragmentID = 0;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id != showingFragmentID) {
            showingFragmentID = id;
            Fragment fragmentToShow = null;
            switch (id) {
                case R.id.navButtonInternet:
                    fragmentToShow = new TcpIpConnectorFragment();
                    break;
                case R.id.navButtonBluetooth:
                    fragmentToShow = new BluetoothConnectorFragment();
                    break;
            }
            //Log.i(this.getClass().getSimpleName(), "Manually Added Fragment");
            getChildFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.ConnectorFragmentContainer, fragmentToShow).commit();
        }
        return true;
    }
}
