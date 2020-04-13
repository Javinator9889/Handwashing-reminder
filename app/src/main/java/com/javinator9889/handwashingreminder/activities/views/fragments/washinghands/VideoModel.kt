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
 * Created by Javinator9889 on 13/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.activities.views.fragments.washinghands

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.utils.Videos.URI.FILENAME
import com.javinator9889.handwashingreminder.utils.Videos.URI.HASH
import com.javinator9889.handwashingreminder.utils.Videos.URI.URL
import com.javinator9889.handwashingreminder.utils.Videos.URI.VideoList
import kotlinx.coroutines.*
import java.io.*
import java.net.URL
import java.security.MessageDigest

class VideoModel : ViewModel() {
    private val cachePath: File =
        HandwashingApplication.getInstance().applicationContext.cacheDir
    private val tag = VideoModel::class.simpleName
    val videos: LiveData<List<File>> = liveData {
        emit(loadVideos())
    }

    private suspend fun loadVideos(): List<File> {
        var files: List<File> = emptyList()
        coroutineScope {
            val deferreds = arrayListOf<Deferred<File>>()
            for (video in VideoList) {
                val url = video[URL]
                val hash = video[HASH]
                val filename = video[FILENAME]
                if (url == null || hash == null || filename == null)
                    continue
                deferreds.add(async { downloadVideo(url, hash, filename) })
            }
            files = deferreds.awaitAll().toList()
        }
        return files
    }

    private suspend fun downloadVideo(
        url: String,
        hash: String,
        name: String
    ): File {
        val file = File(cachePath, name)
        withContext(Dispatchers.IO) {
            val fileData = ByteArrayOutputStream()
            val digest = MessageDigest.getInstance("SHA-256")
            /*do {*/
                if (!needsToDownloadFile(file, hash))
                    return@withContext
                else
                    file.createNewFile()
                val stream = with(URL(url)) {
                    openStream()
                }.let { BufferedInputStream(it, 8192) }
                val data = ByteArray(1024)
                val output = FileOutputStream(file)
                try {
                    var count = stream.read(data)
                    while (count != -1) {
                        output.write(data, 0, count)
                        fileData.write(data, 0, count)
                        count = stream.read(data)
                    }
                    output.flush()
                } catch (e: IOException) {
                    Log.e(tag, "Unable to download video $name", e)
                } finally {
                    output.close()
                    stream.close()
                }
            /*} while (!digest.digest(fileData.toByteArray())
                    .contentEquals(hash.toByteArray())
            )*/
        }
        return file
    }

    private fun needsToDownloadFile(file: File, hash: String): Boolean {
        if (!file.exists())
            return true
        val digest = MessageDigest.getInstance("SHA-256")
        return try {
            val reader = FileInputStream(file)
            val bytes = reader.readBytes()
            !digest.digest(bytes).contentEquals(hash.toByteArray())
        } catch (_: Exception) {
            false
        }
    }
}