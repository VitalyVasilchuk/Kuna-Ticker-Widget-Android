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
package apps.basilisk.kunatickerwidget.billing.skulist.row.delegate;

import com.android.billingclient.api.BillingClient.SkuType;

import apps.basilisk.kunatickerwidget.R;
import apps.basilisk.kunatickerwidget.billing.BillingConstants;
import apps.basilisk.kunatickerwidget.billing.BillingProvider;
import apps.basilisk.kunatickerwidget.billing.skulist.row.RowViewHolder;
import apps.basilisk.kunatickerwidget.billing.skulist.row.SkuRowData;


/**
 * Handles Ui specific to "premium" - non-consumable in-app item row
 */
public class DayDelegate extends UiManagingDelegate {
    public static final String SKU_ID = BillingConstants.SKU_PRIVATE_API_DAY;

    public DayDelegate(BillingProvider billingProvider) {
        super(billingProvider);
    }

    @Override
    public @SkuType
    String getType() {
        return SkuType.INAPP;
    }

    @Override
    public void onBindViewHolder(SkuRowData data, RowViewHolder holder) {
        super.onBindViewHolder(data, holder);
        int textId = mBillingProvider.isDayPurchased() ? R.string.button_own : R.string.button_buy;
        holder.button.setText(textId);
        holder.skuIcon.setImageResource(R.drawable.private_api_day);
    }

    @Override
    public void onButtonClicked(SkuRowData data) {
        if (data != null && mBillingProvider.isDayPurchased()) {
            showAlreadyPurchasedToast();
        }  else {
            super.onButtonClicked(data);
        }
    }
}

