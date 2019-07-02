package c.jahhow.remotecontroller;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TouchPadFragment extends Fragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MainActivity mainActivity = (MainActivity) getActivity();
		TouchPadLayout touchPadLayout = (TouchPadLayout) inflater.inflate(R.layout.touchpad_layout, container, false);
		touchPadLayout.Initialize(mainActivity);
		return touchPadLayout;
	}
}
