package c.jahhow.remotecontroller;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class RemoteControllerApp extends Application implements SkuDetailsResponseListener, PurchasesUpdatedListener {
    private static final String
            ManagePlaySubsUrl = "https://play.google.com/store/account/subscriptions?package=c.jahhow.remotecontroller&sku=subscription.full_access";
    private static final String Sku_Subscription_FullAccess = "subscription.full_access";

    private final List<String> skuList = new ArrayList<>(1);
    BillingClient billingClient = null;
    SkuDetails skuDetails = null;
    int purchaseState = UNSPECIFIED_STATE;
    boolean purchaseSkipped = false;

    ControllerSwitcherFragment controllerSwitcherFragment = null;
    private FetchSkuListener fetchSkuListener = null;

    @Override
    public void onCreate() {
        super.onCreate();
        skuList.add(Sku_Subscription_FullAccess);
        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
            billingClient.startConnection(billingClientStateListener);
        }
    }

    private final BillingClientStateListener billingClientStateListener = new BillingClientStateListener() {
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

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        purchaseState = UNSPECIFIED_STATE;
        if (billingResult.getResponseCode() == BillingResponseCode.OK) {
            if (purchases != null && purchases.size() == 1) {
                Purchase purchase = purchases.get(0);
                if (purchase.getSku().equals(Sku_Subscription_FullAccess)) {
                    purchaseState = purchase.getPurchaseState();
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

    interface FetchSkuListener {
        void onSkuDetailsResponse();
    }

    @Override
    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
        if (billingResult.getResponseCode() == BillingResponseCode.OK) {
            if (skuDetailsList.size() == 1) {
                SkuDetails skuDetails = skuDetailsList.get(0);
                if (Sku_Subscription_FullAccess.equals(skuDetails.getSku())) {
                    this.skuDetails = skuDetails;
                }
            }
        }/* else {
			Log.i(RemoteControllerApp.class.getSimpleName(),
					"onSkuDetailsResponse Not ok getResponseCode() == "
					+ billingResult.getResponseCode() + " : " + billingResult.getDebugMessage());
		}*/

        if (fetchSkuListener != null) {
            fetchSkuListener.onSkuDetailsResponse();
        }
    }

    void SyncPurchase() {
        purchaseState = UNSPECIFIED_STATE;
        Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(SkuType.SUBS);
        if (purchasesResult.getResponseCode() == BillingResponseCode.OK) {
            List<Purchase> purchases = purchasesResult.getPurchasesList();
            if (purchases.size() == 0) {
                // Fetch skuDetails
                if (skuDetails == null)
                    billingClient.querySkuDetailsAsync(
                            SkuDetailsParams.newBuilder().setType(SkuType.SUBS).setSkusList(skuList).build(), this);
            } else {
                Purchase purchase = purchases.get(0);
                if (purchase.getSku().equals(Sku_Subscription_FullAccess)) {
                    purchaseState = purchase.getPurchaseState();
                }
            }
        }
    }

    void OpenPlayStoreManageSubscription(AppCompatActivity activity) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ManagePlaySubsUrl)));
    }

    public void setFetchSkuListener(FetchSkuListener fetchSkuListener) {
        this.fetchSkuListener = fetchSkuListener;
    }

    public void removeFetchSkuListener(FetchSkuListener fetchSkuListener) {
        if (this.fetchSkuListener == fetchSkuListener)
            this.fetchSkuListener = null;
    }
}