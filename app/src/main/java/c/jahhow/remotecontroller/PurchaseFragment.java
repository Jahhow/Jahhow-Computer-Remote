package c.jahhow.remotecontroller;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase.PurchaseState;

public class PurchaseFragment extends Fragment {
	Activity activity;
	ControllerSwitcherFragment controllerSwitcherFragment;
	RemoteControllerApp remoteControllerApp;
	Button purchaseButton;
	TextView subtitle;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.purchase, container, false);
		activity = getActivity();
		assert activity != null;
		remoteControllerApp = (RemoteControllerApp) activity.getApplication();
		subtitle = view.findViewById(R.id.purchaseDescription);
		purchaseButton = view.findViewById(R.id.purchaseButton);
		if (remoteControllerApp.skuDetailsFullAccess != null) {
			SetUiForNotPurchased();
		} else {
			remoteControllerApp.fetchFullAccessSkuListener = new RemoteControllerApp.FetchFullAccessSkuListener() {
				@Override
				public void onSkuDetailsResponse() {
					// This is called only when SyncPurchase = UNSPECIFIED_STATE

					if (remoteControllerApp.skuDetailsFullAccess != null) {
						SetUiForNotPurchased();
					} else {
						Toast.makeText(getContext(), R.string.FailedToReachGooglePlay, Toast.LENGTH_SHORT).show();
					}
				}
			};
			purchaseButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					remoteControllerApp.SyncPurchase();
					switch (remoteControllerApp.fullAccessState) {
						case PurchaseState.PENDING:
							SetUiForNotPurchased();
							remoteControllerApp.OpenPlayStoreManageSubscription(activity);
							break;
						case PurchaseState.PURCHASED:
							controllerSwitcherFragment.OnPurchaseStateChanged();
							break;
					}
				}
			});
		}
		return view;
	}

	void SetUiForNotPurchased() {
		subtitle.setText(R.string.Annual_Subscription);
		purchaseButton.setText(R.string.start);
		purchaseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (remoteControllerApp.fullAccessState == PurchaseState.PENDING)
					remoteControllerApp.OpenPlayStoreManageSubscription(activity);
				else
					remoteControllerApp.billingClient.launchBillingFlow(activity, BillingFlowParams.newBuilder().setSkuDetails(remoteControllerApp.skuDetailsFullAccess).build());
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		remoteControllerApp.fetchFullAccessSkuListener = null;
	}
}