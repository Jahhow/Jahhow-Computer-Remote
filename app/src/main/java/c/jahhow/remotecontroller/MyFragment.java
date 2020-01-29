package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MyFragment extends Fragment {
    private boolean called_onCreateView = false;
    private boolean has_savedInstanceState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        has_savedInstanceState = savedInstanceState != null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        called_onCreateView = true;
        super.onViewCreated(view, savedInstanceState);
    }

    boolean isNotRestoringState() {
        return !called_onCreateView && !has_savedInstanceState;
    }
}
