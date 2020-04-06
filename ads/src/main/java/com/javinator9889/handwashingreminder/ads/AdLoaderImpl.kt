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
 * Created by Javinator9889 on 1/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.ads

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Keep
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.javinator9889.handwashingreminder.gms.ads.AdLoader
import com.google.android.gms.ads.AdLoader as AdBase


const val ADMOB_APP_ID = "ca-app-pub-5517327035817913~5915164054"
const val ADMOB_APP_NATIVE_ID = "ca-app-pub-5517327035817913/5656089851"
const val ADMOB_APP_NATIVE_TEST_ID = "ca-app-pub-3940256099942544/2247696110"

@Keep
class AdLoaderImpl private constructor(context: Context?) : AdLoader {
    private val adOptions: NativeAdOptions
    private var currentNativeAd: UnifiedNativeAd? = null

    init {
        if (context == null)
            throw NullPointerException(
                "Context cannot be null when creating " +
                        "the first instance"
            )
        MobileAds.initialize(context, ADMOB_APP_ID)
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
    }

    @Keep
    companion object Provider : AdLoader.Provider {
        private var instance: AdLoaderImpl? = null

        override fun instance(context: Context?): AdLoader {
            Log.i(
                "AdLoaderImpl", "Instance called - context null? ${context
                        == null}"
            )
            this.instance = instance ?: AdLoaderImpl(context)
            return instance!!
        }
    }

    override fun loadAdForViewGroup(view: ViewGroup, removeAllViews: Boolean) {
        val adLoader = AdBase.Builder(view.context, ADMOB_APP_NATIVE_TEST_ID)
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->
                val adView = LayoutInflater.from(view.context)
                    .inflate(R.layout.native_ad_view, null) as
                        UnifiedNativeAdView
                populateUnifiedNativeAdView(ad, adView)
                if (removeAllViews)
                    view.removeAllViews()
                view.addView(adView)
            }
            .withNativeAdOptions(adOptions)
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    Toast.makeText(
                        view.context, "Failed to load native ad - err. " +
                                "code: $errorCode", Toast.LENGTH_LONG
                    ).show()
                }
            }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun destroy() {
        currentNativeAd?.destroy()
    }

    private fun populateUnifiedNativeAdView(
        nativeAd: UnifiedNativeAd,
        adView: UnifiedNativeAdView
    ) {
        currentNativeAd?.destroy()
        currentNativeAd = nativeAd

        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating =
                nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
}