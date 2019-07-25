package c.jahhow.remotecontroller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TouchPadFragment extends Fragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MainActivity mainActivity = (MainActivity) getActivity();
		TouchPadView touchPadView = (TouchPadView) inflater.inflate(R.layout.touchpad_layout, container, false);
		touchPadView.Initialize(mainActivity);
		return touchPadView;
	}
}
