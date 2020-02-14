package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class SwipeControllerFragment extends Fragment {
    //static final String TAG = SwipeControllerFragment.class.getSimpleName();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.i(TAG, "savedInstanceState " + (savedInstanceState == null ? "==" : "!=") + " null");
        MainActivity mainActivity = (MainActivity) getActivity();
        return new SwipeDetectorLayout(mainActivity);
    }
}