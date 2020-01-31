package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MyFragment extends Fragment {
    private boolean called_onCreateView = false;
    private boolean has_savedInstanceState;

    @Override
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        has_savedInstanceState = savedInstanceState != null;
    }

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        called_onCreateView = true;
        super.onViewCreated(view, savedInstanceState);
    }

    // If you previously have any child fragment added,
    // this can be used in onCreateView() to determine
    // whether there is any child fragment created automatically by the Android Framework
    boolean hasNoChildFragment() {
        //Log.i(getClass().getSimpleName(), "hasNoChildFragment() => " + getChildFragmentManager().getFragments().isEmpty());
        return getChildFragmentManager().getFragments().isEmpty();
    }

    // Use this only if you can't use hasNoChildFragment() to determine the restoring state
    boolean isNotRestoringState() {
        return !called_onCreateView && !has_savedInstanceState;
    }
}
