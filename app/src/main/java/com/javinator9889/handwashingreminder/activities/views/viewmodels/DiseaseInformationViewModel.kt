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
 * Created by Javinator9889 on 19/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.viewmodels

import android.os.Parcel
import android.os.Parcelable
import android.text.Spanned
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.beust.klaxon.Klaxon
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.javinator9889.handwashingreminder.collections.DiseasesInformation
import com.javinator9889.handwashingreminder.collections.DiseasesList
import com.javinator9889.handwashingreminder.collections.DiseasesListWrapper
import com.javinator9889.handwashingreminder.utils.RemoteConfig.Keys.DISEASES_JSON
import kotlinx.coroutines.*
import org.sufficientlysecure.htmltextview.HtmlFormatter
import org.sufficientlysecure.htmltextview.HtmlFormatterBuilder

private const val DATA_KEY = "text:html:text"
private const val PARSED_JSON_KEY = "text:json:parsed"

class DiseaseInformationViewModel(
    private val state: SavedStateHandle
) : ViewModel() {
    val parsedHTMLText: LiveData<List<ParsedHTMLText>> = liveData {
        emitSource(state.getLiveData(DATA_KEY, parseHTML()))
    }

    private suspend fun parseHTML(): List<ParsedHTMLText> {
        val informationList = withContext(Dispatchers.IO) {
            if (state.contains(PARSED_JSON_KEY) &&
                state.get<List<DiseasesInformation>>(PARSED_JSON_KEY) != null
            )
                DiseasesList(
                    state.get<List<DiseasesInformation>>(PARSED_JSON_KEY)!!
                )
            val diseasesString =
                with(Firebase.remoteConfig) {
                    getString(DISEASES_JSON)
                }
            Klaxon().parse<DiseasesList>(diseasesString)
        } ?: return emptyList()
        state.set(
            PARSED_JSON_KEY,
            DiseasesListWrapper(informationList.diseases)
        )
        return withContext(Dispatchers.Default) {
            val parsedItemsList =
                ArrayList<ParsedHTMLText>(informationList.diseases.size)
            val deferreds =
                ArrayList<List<Deferred<Spanned>>>(informationList.diseases.size)
            informationList.diseases.forEach { information ->
                val htmlDef = listOf(
                    async { createHTML(information.name) },
                    async { createHTML(information.shortDescription) },
                    async { createHTML(information.longDescription) },
                    async { createHTML(information.provider) },
                    async { createHTML(information.website) },
                    async { createHTML(information.symptoms) },
                    async { createHTML(information.prevention) }
                )
                deferreds.add(htmlDef)
            }
            deferreds.forEachIndexed { i, infoList ->
                val data = infoList.awaitAll()
                parsedItemsList.add(
                    i, ParsedHTMLText(
                        data[0],
                        data[1],
                        data[2],
                        data[3],
                        data[4],
                        data[5],
                        data[6]
                    )
                )
            }
            state.set(DATA_KEY, parsedItemsList)
            parsedItemsList
        }
    }

    private fun createHTML(htmlText: String): Spanned =
        with(HtmlFormatterBuilder()) {
            html = htmlText
            isRemoveTrailingWhiteSpace = true
            HtmlFormatter.formatHtml(this)
        }
}

class DiseaseInformationFactory :
    ViewModelAssistedFactory<DiseaseInformationViewModel> {
    override fun create(handle: SavedStateHandle) =
        DiseaseInformationViewModel(handle)
}

data class ParsedHTMLText(
    val name: CharSequence,
    val shortDescription: CharSequence,
    val longDescription: CharSequence,
    val provider: CharSequence,
    val website: CharSequence,
    val symptoms: CharSequence,
    val prevention: CharSequence
) : Parcelable {
    constructor(parcel: Parcel) : this(
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        TextUtils.writeToParcel(name, parcel, flags)
        TextUtils.writeToParcel(shortDescription, parcel, flags)
        TextUtils.writeToParcel(longDescription, parcel, flags)
        TextUtils.writeToParcel(provider, parcel, flags)
        TextUtils.writeToParcel(website, parcel, flags)
        TextUtils.writeToParcel(symptoms, parcel, flags)
        TextUtils.writeToParcel(prevention, parcel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParsedHTMLText> {
        override fun createFromParcel(parcel: Parcel): ParsedHTMLText {
            return ParsedHTMLText(parcel)
        }

        override fun newArray(size: Int): Array<ParsedHTMLText?> {
            return arrayOfNulls(size)
        }
    }
}