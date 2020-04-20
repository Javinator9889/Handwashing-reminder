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
package com.javinator9889.handwashingreminder.activities.views.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.perf.metrics.AddTrace
import com.javinator9889.handwashingreminder.application.HandwashingApplication
import com.javinator9889.handwashingreminder.utils.Videos.URI.FILENAME
import com.javinator9889.handwashingreminder.utils.Videos.URI.HASH
import com.javinator9889.handwashingreminder.utils.Videos.URI.URL
import com.javinator9889.handwashingreminder.utils.Videos.URI.VideoList
import com.javinator9889.handwashingreminder.utils.isConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.*
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject

private const val LIVEDATA_KEY = "videomodel:livedata"

class VideoModel(
    private val state: SavedStateHandle, private val position: Int
) : ViewModel() {
    private val cachePath: File =
        HandwashingApplication.getInstance().applicationContext.cacheDir
    val videos: LiveData<String> = liveData {
        emitSource(state.getLiveData(LIVEDATA_KEY, loadVideo()))
    }

    private suspend fun loadVideo(): String {
        var file = File("nonexistent")
        Timber.d("Creating a new instance")
        coroutineScope {
            val video = VideoList[position]
            val url = video[URL]
            val hash = video[HASH]
            val filename = video[FILENAME]
            if (url != null && hash != null && filename != null) {
                file = downloadVideo(url, hash, filename)
            }
        }
        var isVideoDownloaded = true
        if (!file.exists())
            isVideoDownloaded = false
        if (isVideoDownloaded)
            state.set(LIVEDATA_KEY, file.name)
        return file.name
    }

    @AddTrace(name = "videoDownload")
    private suspend fun downloadVideo(
        url: String,
        hash: String,
        name: String
    ): File {
        val file = File(cachePath, name)
        withContext(Dispatchers.IO) {
            val digest = MessageDigest.getInstance("SHA-256")
            do {
                if (!isConnected() || !needsToDownloadFile(file, hash))
                    return@withContext
                else
                    file.createNewFile()
                val stream = with(URL(url)) {
                    openStream()
                }.let { BufferedInputStream(it, 8192) }
                val data = ByteArray(8192)
                val output = FileOutputStream(file)
                try {
                    var count = stream.read(data)
                    while (count > 0) {
                        output.write(data, 0, count)
                        digest.update(data, 0, count)
                        count = stream.read(data)
                    }
                    output.flush()
                } catch (e: IOException) {
                    Timber.e(e, "Unable to download video $name")
                } finally {
                    output.close()
                    stream.close()
                }
            } while (!sameSHA2Hash(hash, digest))
        }
        return file
    }

    private fun needsToDownloadFile(file: File, hash: String): Boolean {
        if (!file.exists())
            return true
        val digest = MessageDigest.getInstance("SHA-256")
        val reader: InputStream = FileInputStream(file)
        return try {
            val bytes = reader.readBytes()
            digest.update(bytes)
            sameSHA2Hash(hash, digest)
        } catch (_: Exception) {
            false
        } finally {
            reader.close()
        }
    }

    private fun sameSHA2Hash(
        hash: String,
        messageDigest: MessageDigest
    ): Boolean {
        val sha2sum = messageDigest.digest()
        val bigInt = BigInteger(1, sha2sum)
        val obtainedHash = String.format("%064x", bigInt)
        return obtainedHash.equals(hash, true)
    }
}

class VideoModelFactory @Inject constructor(private val position: Int) :
    ViewModelAssistedFactory<VideoModel> {
    override fun create(handle: SavedStateHandle) = VideoModel(handle, position)
}