package c.jahhow.remotecontroller;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TouchPadFragment extends Fragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MainActivity mainActivity = (MainActivity) getActivity();
		TouchPadView touchPadView = (TouchPadView) inflater.inflate(R.layout.touchpad_layout, container, false);
		assert mainActivity != null;
		touchPadView.Initialize(mainActivity);
		return touchPadView;
	}
}
