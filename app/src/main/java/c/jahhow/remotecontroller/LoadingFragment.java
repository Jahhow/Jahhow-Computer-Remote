package c.jahhow.remotecontroller;

import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LoadingFragment extends Fragment {
    private CharSequence loadingText;

    public LoadingFragment() {
        this(null);
    }

    LoadingFragment(@StringRes int id) {
        loadingText = getResources().getText(id);
    }

    LoadingFragment(CharSequence loadingText) {
        this.loadingText = loadingText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_fragment, container, false);
        if (loadingText != null) {
            TextView textView = view.findViewById(R.id.textLoading);
            textView.setText(loadingText);
        }
        return view;
    }
}
