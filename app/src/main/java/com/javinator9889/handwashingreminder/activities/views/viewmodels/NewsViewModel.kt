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
 * Created by Javinator9889 on 3/06/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.javinator9889.handwashingreminder.collections.*
import com.javinator9889.handwashingreminder.firebase.Auth
import com.javinator9889.handwashingreminder.network.HttpDownloader
import com.javinator9889.handwashingreminder.utils.API_URL
import javinator9889.localemanager.utils.languagesupport.LanguagesSupport.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import timber.log.Timber
import java.io.Reader
import java.util.*


class NewsViewModel : ViewModel() {
    val newsData: MutableLiveData<NewsData> = MutableLiveData()

    suspend fun populateData(
        from: Int = 0,
        amount: Int = 10,
        @Language language: String = Language.ENGLISH
    ) {
        try {
            val httpRequest = HttpDownloader()
            val klaxon = with(Klaxon()) {
                propertyStrategy(newsStrategy)
                fieldConverter(KlaxonDate::class, dateConverter)
                fieldConverter(KlaxonElements::class, elementConverter)
            }
            var requestReader: Reader?
            Auth.init()
            val token = Auth.token()
            Timber.d("Auth token: $token")
            withContext(Dispatchers.IO) {
                requestReader = httpRequest.json(
                    "${API_URL}/api/v1?from=$from&amount=$amount&lang=$language",
                    headers = Headers.of(mapOf("Authorization" to "Bearer $token"))
                )
            }
            withContext(Dispatchers.Default) {
                JsonReader(requestReader!!).use { reader ->
                    reader.beginArray {
                        while (reader.hasNext()) {
                            newsData.postValue(klaxon.parse<NewsData>(reader))
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Timber.w(e, "Exception while populating data")
            withContext(Dispatchers.Main) {
                newsData.value = NewsData(
                    hasError = true,
                    id = "",
                    discoverDate = Date(),
                    title = "",
                    text = "",
                    url = ""
                )
            }
        }
    }
}