package c.jahhow.remotecontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SwipeControllerFragment extends Fragment {
	private SwipeDetectorLayout swipeDetectorLayout;
	private MainActivity mainActivity;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		swipeDetectorLayout = new SwipeDetectorLayout(mainActivity);
		return swipeDetectorLayout;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		SharedPreferences preferences = mainActivity.preferences;
		if(!swipeDetectorLayout.demoing&& !preferences.contains(MainActivity.KeyPrefer_Swiped))
			preferences.edit().putBoolean(MainActivity.KeyPrefer_Swiped, true).apply();
	}
}
