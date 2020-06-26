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

import android.text.Spanned
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beust.klaxon.Klaxon
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.javinator9889.handwashingreminder.collections.DiseasesInformation
import com.javinator9889.handwashingreminder.collections.DiseasesList
import com.javinator9889.handwashingreminder.data.ParsedHTMLText
import com.javinator9889.handwashingreminder.utils.RemoteConfig.DISEASES_JSON
import com.javinator9889.handwashingreminder.utils.notNull
import kotlinx.coroutines.*
import org.sufficientlysecure.htmltextview.HtmlFormatter
import org.sufficientlysecure.htmltextview.HtmlFormatterBuilder
import timber.log.Timber

private const val DATA_KEY = "text:html:text"
private const val PARSED_JSON_KEY = "text:json:parsed"

class DiseaseInformationViewModel(
    private val state: SavedStateHandle
) : ViewModel() {
    private val informationList: DiseasesList = loadHtmlData()
    val parsedHTMLText: MutableLiveData<List<ParsedHTMLText>> =
        state.getLiveData(DATA_KEY, emptyList())

    private fun loadHtmlData(): DiseasesList {
        if (state.contains(PARSED_JSON_KEY) &&
            state.get<List<DiseasesInformation>>(PARSED_JSON_KEY) != null
        )
            return DiseasesList(state.get(PARSED_JSON_KEY)!!)
        val diseasesString = with(Firebase.remoteConfig) {
            getString(DISEASES_JSON)
        }
        return Klaxon().parse<DiseasesList>(diseasesString)
            ?: DiseasesList(emptyList())
    }

    fun parseHtml() = viewModelScope.launch {
        Timber.d("Parsing HTML")
        if (!state.get<List<ParsedHTMLText>>(DATA_KEY).isNullOrEmpty())
            return@launch
        val parsedItemsList =
            ArrayList<ParsedHTMLText>(informationList.diseases.size)
        val deferreds = mutableListOf<Collection<Deferred<Spanned>>>()
        informationList.diseases.forEach { disease ->
            deferreds.add(
                listOf(
                    async { createHTML(disease.name) },
                    async { createHTML(disease.shortDescription) },
                    async { createHTML(disease.longDescription) },
                    async { createHTML(disease.provider) },
                    async { createHTML(disease.website) },
                    async { createHTML(disease.symptoms) },
                    async { createHTML(disease.prevention) }
                )
            )
        }
        deferreds.forEachIndexed { i, htmlData ->
            launch {
                val data = htmlData.awaitAll()
                parsedItemsList.add(
                    i, ParsedHTMLText(
                        name = data[0],
                        shortDescription = data[1],
                        longDescription = data[2],
                        provider = data[3],
                        website = data[4],
                        symptoms = data[5],
                        prevention = data[6]
                    )
                )
                withContext(Dispatchers.Main) {
                    state[DATA_KEY] = parsedItemsList
                }
            }.invokeOnCompletion {
                it.notNull {
                    viewModelScope.launch(context = Dispatchers.Main) {
                        state[DATA_KEY] = parsedItemsList
                        parsedHTMLText.value = parsedItemsList
                    }
                }
            }
        }
    }

    private fun createHTML(htmlText: String): Spanned =
        with(HtmlFormatterBuilder()) {
            html = htmlText
            isRemoveTrailingWhiteSpace = true
            HtmlFormatter.formatHtml(this)
        }

    companion object Factory :
        ViewModelAssistedFactory<DiseaseInformationViewModel> {
        override fun create(handle: SavedStateHandle) =
            DiseaseInformationViewModel(handle)
    }
}