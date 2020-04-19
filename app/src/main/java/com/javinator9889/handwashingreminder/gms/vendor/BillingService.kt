/*
 * Copyright © 2020 - present | Handwashing reminder by Javinator9889
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 * Created by Javinator9889 on 17/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.gms.vendor

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.javinator9889.handwashingreminder.R
import com.javinator9889.handwashingreminder.listeners.OnPurchaseFinishedListener
import com.javinator9889.handwashingreminder.utils.isDebuggable
import timber.log.Timber
import java.lang.ref.WeakReference

class BillingService(private val context: Context) : PurchasesUpdatedListener {
    private val billingClient: BillingClient =
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    private lateinit var skuDetailsMap: HashMap<String, SkuDetails>
    private var arePurchasesAvailable = false
    private val purchaseFinishedListener =
        ArrayList<WeakReference<OnPurchaseFinishedListener>>()

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingResponseCode.OK
                ) {
                    arePurchasesAvailable = true
                    querySkuDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                billingClient.startConnection(this)
            }
        })
    }

    private fun querySkuDetails() {
        val skuList = if (isDebuggable())
            context.resources.getStringArray(R.array.in_app_donations_debug)
        else
            context.resources.getStringArray(R.array.in_app_donations)
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList.asList())
            .setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build()) { result, skuDetailsList ->
            if (result.responseCode == BillingResponseCode.OK &&
                skuDetailsList != null
            ) {
                skuDetailsMap = HashMap(skuDetailsList.size)
                for (skuDetail in skuDetailsList) {
                    skuDetailsMap[skuDetail.sku] = skuDetail
                }
            } else {
                Timber.e(
                    "Error in billing client: ${result
                        .responseCode} - message: ${result.debugMessage}"
                )
            }
        }
    }

    fun addOnPurchaseFinishedListener(listener: OnPurchaseFinishedListener) {
        purchaseFinishedListener.add(WeakReference(listener))
    }

    fun doPurchase(productId: String, activity: Activity): BillingResult {
        if (!arePurchasesAvailable)
            return BillingResult.newBuilder().setResponseCode(
                BillingResponseCode.BILLING_UNAVAILABLE
            ).build()
        val skuDetails = skuDetailsMap[productId]
            ?: return BillingResult.newBuilder()
                .setResponseCode(BillingResponseCode.ITEM_UNAVAILABLE)
                .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        return billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingResponseCode.OK &&
            purchases != null
        ) {
            for (purchase in purchases)
                handlePurchase(purchase)
        } else if (billingResult.responseCode == BillingResponseCode
                .USER_CANCELED
        ) {
            for (listener in purchaseFinishedListener)
                listener.get()?.onPurchaseFinished(
                    resultCode = BillingResponseCode.USER_CANCELED
                )
        } else {
            Timber.e(
                "Purchase failed - error code: ${billingResult
                    .responseCode}| ${billingResult.debugMessage}"
            )
            for (listener in purchaseFinishedListener)
                listener.get()?.onPurchaseFinished(
                    resultCode = BillingResponseCode.ERROR
                )
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val params = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.consumeAsync(params) { billingResult, token ->
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        for (listener in purchaseFinishedListener)
                            listener.get()?.onPurchaseFinished(
                                token,
                                BillingResponseCode.OK
                            )
                    }
                }
            }
        }
    }
}