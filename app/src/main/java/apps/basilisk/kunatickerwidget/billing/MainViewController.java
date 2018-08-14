package apps.basilisk.kunatickerwidget.billing;

import android.util.Log;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;

import java.util.List;

import apps.basilisk.kunatickerwidget.BuildConfig;
import apps.basilisk.kunatickerwidget.activity.MainActivity;
import apps.basilisk.kunatickerwidget.billing.BillingManager.BillingUpdatesListener;
import apps.basilisk.kunatickerwidget.billing.skulist.row.delegate.DayDelegate;
import apps.basilisk.kunatickerwidget.billing.skulist.row.delegate.OneMonthDelegate;
import apps.basilisk.kunatickerwidget.billing.skulist.row.delegate.SixMonthsDelegate;
import apps.basilisk.kunatickerwidget.billing.skulist.row.delegate.ThreeMonthsDelegate;
import apps.basilisk.kunatickerwidget.billing.skulist.row.delegate.UnlimitedDelegate;
import apps.basilisk.kunatickerwidget.billing.skulist.row.delegate.WeekDelegate;
import apps.basilisk.kunatickerwidget.billing.skulist.row.delegate.YearDelegate;

/**
 * Handles control logic of the MainActivity
 */
public class MainViewController {
    private static final String TAG = "MainViewController";

    private final UpdateListener mUpdateListener;
    private MainActivity mActivity;

    // Tracks if we currently own subscriptions SKUs
    private boolean mWeek;
    private boolean mOneMonth;
    private boolean mThreeMonts;
    private boolean mSixMonths;
    private boolean mYear;


    // Tracks if we currently own a IN-APP
    private boolean mUnlimited;

    private String subscriptionSkuId;

    public MainViewController(MainActivity activity) {
        mUpdateListener = new UpdateListener();
        mActivity = activity;
        subscriptionSkuId = "";
    }

    public UpdateListener getUpdateListener() {
        return mUpdateListener;
    }

    public boolean isWeekSubscribed() {
        return mWeek;
    }

    public boolean isOneMonthSubscribed() {
        return mOneMonth;
    }

    public boolean isThreeMontsSubscribed() {
        return mThreeMonts;
    }

    public boolean isSixMonthsSubscribed() {
        return mSixMonths;
    }

    public boolean isYearSubscribed() {
        return mYear;
    }

    public boolean isUnlimited() {
        return mUnlimited;
    }

    public boolean isDayPurchased() {
        // todo реализовать обработку купленного дня
        return true;
    }

    public String getSubscriptionSkuId() {
        return subscriptionSkuId;
    }

    /**
     * Handler to billing updates
     */
    private class UpdateListener implements BillingUpdatesListener {

        @Override
        public void onBillingClientSetupFinished() {
            mActivity.onBillingManagerSetupFinished();
        }

        @Override
        public void onConsumeFinished(String token, @BillingResponse int result) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Consumption finished. Purchase token: " + token + ", result: " + result);

            if (result == BillingResponse.OK) {
                switch (token) {
                    case "": break;
                }

                Log.d(TAG, "Consumption successful. Provisioning.");
            } else {
                // basilisk mActivity.alert(R.string.alert_error_consuming, result);
            }

            mActivity.showRefreshedUi();
        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {
            mWeek = false;
            mOneMonth = false;
            mThreeMonts = false;
            mSixMonths = false;
            mYear = false;
            mUnlimited = false;
            subscriptionSkuId = "";

            for (Purchase purchase : purchaseList) {
                switch (purchase.getSku()) {
                    // todo переделать на использоание BillingConstants
                    case UnlimitedDelegate.SKU_ID:
                        mUnlimited = true;
                        //mActivity.getBillingManager().consumeAsync(purchase.getPurchaseToken());
                        mActivity.setPrivateApiPaid();
                        break;

                    case DayDelegate.SKU_ID:
                        // todo We should consume the purchase and предоставить доступ на один день
                        mActivity.getBillingManager().consumeAsync(purchase.getPurchaseToken());
                        mActivity.setPrivateApiPaid();
                        break;

                    case WeekDelegate.SKU_ID:
                        mWeek = true;
                        subscriptionSkuId = WeekDelegate.SKU_ID;
                        mActivity.setPrivateApiPaid();
                        break;
                    case OneMonthDelegate.SKU_ID:
                        mOneMonth = true;
                        subscriptionSkuId = OneMonthDelegate.SKU_ID;
                        mActivity.setPrivateApiPaid();
                        break;
                    case ThreeMonthsDelegate.SKU_ID:
                        mThreeMonts = true;
                        subscriptionSkuId = ThreeMonthsDelegate.SKU_ID;
                        mActivity.setPrivateApiPaid();
                        break;
                    case SixMonthsDelegate.SKU_ID:
                        mSixMonths = true;
                        subscriptionSkuId = SixMonthsDelegate.SKU_ID;
                        mActivity.setPrivateApiPaid();
                        break;
                    case YearDelegate.SKU_ID:
                        mYear = true;
                        subscriptionSkuId = YearDelegate.SKU_ID;
                        mActivity.setPrivateApiPaid();
                        break;
                }
            }
            mActivity.showRefreshedUi();
        }
    }

}