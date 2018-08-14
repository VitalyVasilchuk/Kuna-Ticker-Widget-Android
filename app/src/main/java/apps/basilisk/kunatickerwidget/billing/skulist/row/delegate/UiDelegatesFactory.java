package apps.basilisk.kunatickerwidget.billing.skulist.row.delegate;

import com.android.billingclient.api.BillingClient.SkuType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apps.basilisk.kunatickerwidget.billing.BillingProvider;
import apps.basilisk.kunatickerwidget.billing.skulist.row.RowViewHolder;
import apps.basilisk.kunatickerwidget.billing.skulist.row.SkuRowData;

/**
 * This factory is responsible to finding the appropriate delegate for Ui rendering and calling
 * corresponding method on it.
 */
public class UiDelegatesFactory {
    private final Map<String, UiManagingDelegate> uiDelegates;

    public UiDelegatesFactory(BillingProvider provider) {
        uiDelegates = new HashMap<>();
        uiDelegates.put(WeekDelegate.SKU_ID, new WeekDelegate(provider));
        uiDelegates.put(OneMonthDelegate.SKU_ID, new OneMonthDelegate(provider));
        uiDelegates.put(ThreeMonthsDelegate.SKU_ID, new ThreeMonthsDelegate(provider));
        uiDelegates.put(SixMonthsDelegate.SKU_ID, new SixMonthsDelegate(provider));
        uiDelegates.put(YearDelegate.SKU_ID, new YearDelegate(provider));

        uiDelegates.put(DayDelegate.SKU_ID, new DayDelegate(provider));
        uiDelegates.put(UnlimitedDelegate.SKU_ID, new UnlimitedDelegate(provider));
    }

    /**
     * Returns the list of all SKUs for the billing type specified
     */
    public final List<String> getSkuList(@SkuType String billingType) {
        List<String> result = new ArrayList<>();
        for (String skuId : uiDelegates.keySet()) {
            UiManagingDelegate delegate = uiDelegates.get(skuId);
            if (delegate.getType().equals(billingType)) {
                result.add(skuId);
            }
        }
        return result;
    }

    public void onBindViewHolder(SkuRowData data, RowViewHolder holder) {
        uiDelegates.get(data.getSku()).onBindViewHolder(data, holder);
    }

    public void onButtonClicked(SkuRowData data) {
        uiDelegates.get(data.getSku()).onButtonClicked(data);
    }
}
