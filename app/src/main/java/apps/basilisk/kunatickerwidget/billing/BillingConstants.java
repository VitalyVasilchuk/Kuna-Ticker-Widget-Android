/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package apps.basilisk.kunatickerwidget.billing;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.SkuType;

import java.util.Arrays;
import java.util.List;

/**
 * Static fields and methods useful for billing
 */
public final class BillingConstants {
    // SKUs for our products: the UNLIMITED (non-consumable) and DAY (consumable)
    public static final String SKU_PRIVATE_API_DAY = "private_api_day";
    public static final String SKU_PRIVATE_API_UNLIMITED = "private_api_unlimited";

    // SKU for our subscription
    public static final String SKU_PRIVATE_API_1_WEEK = "private_api_1_week";
    public static final String SKU_PRIVATE_API_1_MONTH = "private_api_1_month";
    public static final String SKU_PRIVATE_API_3_MONTHS = "private_api_3_months";
    public static final String SKU_PRIVATE_API_6_MONTHS = "private_api_6_months";
    public static final String SKU_PRIVATE_API_1_YEAR = "private_api_1_year";

    private static final String[] INAPP_SKUS = {SKU_PRIVATE_API_DAY, SKU_PRIVATE_API_UNLIMITED};
    private static final String[] SUBS_SKUS = {
            SKU_PRIVATE_API_1_WEEK, SKU_PRIVATE_API_1_MONTH, SKU_PRIVATE_API_3_MONTHS,
            SKU_PRIVATE_API_6_MONTHS, SKU_PRIVATE_API_1_YEAR
    };

    private BillingConstants(){}

    /**
     * Returns the list of all SKUs for the billing type specified
     */
    public static final List<String> getSkuList(@BillingClient.SkuType String billingType) {
        return (billingType == SkuType.INAPP) ? Arrays.asList(INAPP_SKUS) : Arrays.asList(SUBS_SKUS);
    }
}

