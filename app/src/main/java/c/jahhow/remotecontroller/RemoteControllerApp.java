package c.jahhow.remotecontroller;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchaseState;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import static com.android.billingclient.api.Purchase.PurchaseState.UNSPECIFIED_STATE;

public class RemoteControllerApp extends Application {
	ControllerSwitcherFragment controllerSwitcherFragment = null;

	BillingClient billingClient = null;
	SkuDetails skuDetailsFullAccess = null;
	int fullAccessState = UNSPECIFIED_STATE;

	static final String
			ManagePlaySubsUrl = "https://play.google.com/store/account/subscriptions?package=c.jahhow.remotecontroller&sku=subscription.full_access",
			Sku_Subscription_FullAccess = "subscription.full_access";

	/*@Override
	public void onCreate() {
		super.onCreate();
		StartBillingClient();
	}*/

	PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
		@Override
		public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
			fullAccessState = UNSPECIFIED_STATE;
			if (billingResult.getResponseCode() == BillingResponseCode.OK) {
				if (purchases != null && purchases.size() == 1) {
					Purchase purchase = purchases.get(0);
					if (purchase.getSku().equals(Sku_Subscription_FullAccess)) {
						fullAccessState = purchase.getPurchaseState();
						if (purchase.getPurchaseState() == PurchaseState.PURCHASED) {
							if (!purchase.isAcknowledged()) {
								AcknowledgePurchaseParams acknowledgePurchaseParams =
										AcknowledgePurchaseParams.newBuilder()
												.setPurchaseToken(purchase.getPurchaseToken())
												.build();
								billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
									@Override
									@Keep
									public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
									}
								});
							}
						}
					}
				}
			}

			if (controllerSwitcherFragment != null)
				controllerSwitcherFragment.OnPurchaseStateChanged();
		}
	};
	BillingClientStateListener billingClientStateListener = new BillingClientStateListener() {
		@Override
		public void onBillingSetupFinished(BillingResult billingResult) {
			if (billingResult.getResponseCode() == BillingResponseCode.OK) {
				SyncPurchase();
			}
		}

		@Override
		public void onBillingServiceDisconnected() {
		}
	};

	interface FetchFullAccessSkuListener {
		void onSkuDetailsReady();
	}

	FetchFullAccessSkuListener fetchFullAccessSkuListener = null;

	void FetchFullAccessSku() {
		List<String> skuList = new ArrayList<>();
		skuList.add(Sku_Subscription_FullAccess);
		billingClient.querySkuDetailsAsync(
				SkuDetailsParams.newBuilder().setType(SkuType.SUBS).setSkusList(skuList).build(),
				new SkuDetailsResponseListener() {
					@Override
					public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
						if (billingResult.getResponseCode() == BillingResponseCode.OK) {
							if (skuDetailsList.size() == 1) {
								SkuDetails skuDetails = skuDetailsList.get(0);
								if (Sku_Subscription_FullAccess.equals(skuDetails.getSku())) {
									skuDetailsFullAccess = skuDetails;
								}
							}
						}

						if (fetchFullAccessSkuListener != null) {
							fetchFullAccessSkuListener.onSkuDetailsReady();
						}
					}
				}
		);
	}

	void SyncPurchase() {
		fullAccessState = UNSPECIFIED_STATE;
		Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(SkuType.SUBS);
		if (purchasesResult.getResponseCode() == BillingResponseCode.OK) {
			List<Purchase> purchases = purchasesResult.getPurchasesList();
			if (purchases.size() == 0) {
				// Fetch skuDetailsFullAccess
				if (skuDetailsFullAccess == null)
					FetchFullAccessSku();
			} else {
				Purchase purchase = purchases.get(0);
				if (purchase.getSku().equals(Sku_Subscription_FullAccess)) {
					fullAccessState = purchase.getPurchaseState();
				}
			}
		}
	}

	void OpenPlayStoreManageSubscription() {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ManagePlaySubsUrl)));
	}

	void StartBillingClient() {
		Reset();
		billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(purchasesUpdatedListener).build();
		billingClient.startConnection(billingClientStateListener);
	}

	void EndBillingClient() {
		if (billingClient != null) {
			billingClient.endConnection();
			Reset();
		}
	}

	void Reset() {
		controllerSwitcherFragment = null;
		billingClient = null;
		skuDetailsFullAccess = null;
		fullAccessState = UNSPECIFIED_STATE;
	}
}
