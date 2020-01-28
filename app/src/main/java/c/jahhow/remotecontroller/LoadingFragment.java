package c.jahhow.remotecontroller;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LoadingFragment extends Fragment {
    private static final String ARG_LOADING_TEXT = "0";
    private CharSequence loadingText = null;

    public LoadingFragment() {
    }

    LoadingFragment(CharSequence loadingText) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence(ARG_LOADING_TEXT, loadingText);
        setArguments(bundle);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            loadingText = getArguments().getString(ARG_LOADING_TEXT);
        }
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
