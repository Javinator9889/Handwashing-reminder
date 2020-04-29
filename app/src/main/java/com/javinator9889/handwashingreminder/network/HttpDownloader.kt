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
 * Created by Javinator9889 on 21/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.network

import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import java.io.IOException

class HttpDownloader : OkHttpDownloader {
    private val client = OkHttpClient()

    override fun downloadFile(url: String): BufferedSource {
        val request = with(Request.Builder()) {
            url(url)
            cacheControl(CacheControl.FORCE_NETWORK)
            build()
        }
        with(client.newCall(request).execute()) {
            if (!isSuccessful) {
                close()
                throw IOException("Unexpected code $this")
            }
            return body()!!.source()
        }
    }
}