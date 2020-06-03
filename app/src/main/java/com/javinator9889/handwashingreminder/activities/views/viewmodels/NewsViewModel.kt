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
import com.javinator9889.handwashingreminder.collections.NewsData
import com.javinator9889.handwashingreminder.collections.newsStrategy
import com.javinator9889.handwashingreminder.network.HttpDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Reader

class NewsViewModel : ViewModel() {
    val newsData: MutableLiveData<NewsData> = MutableLiveData()

    suspend fun populateData(from: Int = 0, amount: Int = 10) {
        val httpRequest = HttpDownloader()
        val klaxon = Klaxon().propertyStrategy(newsStrategy)
        var requestReader: Reader? = null
        withContext(Dispatchers.IO) {
            requestReader = httpRequest.json("http://0.0.0.0:3000/api/v1/")
        }
        withContext(Dispatchers.Default) {
            JsonReader(requestReader!!).use { reader ->
                var position = 0
                reader.beginArray {
                    while (reader.hasNext()) {
                        if (position < from)
                            continue
                        if (position > from + amount)
                            break
                        newsData.value = klaxon.parse<NewsData>(reader)
                        position++
                    }
                }
            }
        }
    }
}