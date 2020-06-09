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
 * Created by Javinator9889 on 9/06/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import kotlin.IllegalStateException


object Auth {
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var privateToken: String

    suspend fun init() {
        if (!::auth.isInitialized)
            auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val signedIn = Channel<Boolean>(0)
        if (currentUser == null) {
            Timber.d("No user defined lo signing-in")
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    signedIn.offer(task.isSuccessful)
                    if (!task.isSuccessful)
                        Timber.e(task.exception, "Error while logging")
                }
            if (signedIn.receive()) {
                Timber.d("Signing-in successful so saving user")
                this.currentUser = auth.currentUser!!
            }
        } else this.currentUser = currentUser
    }

    suspend fun token(): String {
        if (!::auth.isInitialized)
            throw IllegalStateException("init must be called first")
        if (::privateToken.isInitialized) {
            Timber.d("Obtaining previous generated token")
            return privateToken
        }
        val tokenChannel = Channel<String?>(0)
        currentUser.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    tokenChannel.offer(task.result!!.token!!)
                else
                    tokenChannel.offer(null)
            }
        privateToken = tokenChannel.receive()
            ?: throw IllegalStateException("token not valid")
        return privateToken
    }

    fun logout() {
        if (!::auth.isInitialized)
            throw IllegalStateException("init must be called first")
        auth.signOut()
    }
}