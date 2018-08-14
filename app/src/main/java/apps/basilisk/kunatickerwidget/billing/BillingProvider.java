package apps.basilisk.kunatickerwidget.billing;

/**
 * An interface that provides an access to BillingLibrary methods
 */
public interface BillingProvider {
    BillingManager getBillingManager();

    boolean isDayPurchased();
    boolean isUnlimitedPurchased();

    boolean isWeekSubscribed();
    boolean isOneMonthSubscribed();
    boolean isThreeMonthsSubscribed();
    boolean isSixMonthsSubscribed();
    boolean isYearSubscribed();

    String getSubscriptionSkuId();
}

