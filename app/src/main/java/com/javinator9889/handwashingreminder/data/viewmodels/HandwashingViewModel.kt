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
 * Created by Javinator9889 on 24/06/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.javinator9889.handwashingreminder.data.repositories.HandwashingRepository
import com.javinator9889.handwashingreminder.data.room.db.HandwashingDatabase
import com.javinator9889.handwashingreminder.data.room.entities.Handwashing
import com.javinator9889.handwashingreminder.utils.calendar.CalendarUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class HandwashingViewModel(application: Application) :
    AndroidViewModel(application) {
    private val repository: HandwashingRepository
    val allData: LiveData<List<Handwashing>>

    init {
        val handwashingDao =
            HandwashingDatabase.getDatabase(application).handwashingDao()
        repository = HandwashingRepository(handwashingDao)
        allData = repository.allData
    }

    fun create(handwashing: Handwashing) =
        viewModelScope.launch(Dispatchers.IO) { repository.create(handwashing) }

    fun increment(date: Date) =
        viewModelScope.launch(Dispatchers.IO) { repository.increment(date) }

    fun decrement(date: Date) =
        viewModelScope.launch(Dispatchers.IO) { repository.decrement(date) }

    fun update(handwashing: Handwashing) =
        viewModelScope.launch(Dispatchers.IO) { repository.update(handwashing) }

    fun getAsync(date: Date = CalendarUtils.today.time) =
        viewModelScope.async(Dispatchers.IO) { repository.get(date) }

    fun getBetweenAsync(from: Date, to: Date) =
        viewModelScope.async(Dispatchers.IO) { repository.getBetween(from, to) }

    fun getWeeklyAsync() = getBetweenAsync(
        from = CalendarUtils.lastWeek.time,
        to = CalendarUtils.today.time
    )

    fun getMonthlyAsync() = getBetweenAsync(
        from = CalendarUtils.lastMonth.time,
        to = CalendarUtils.today.time
    )

    fun getTodayCountAsync() = viewModelScope.async {
        return@async getAsync().await()?.amount
    }

    fun getWeeklyCountAsync() = viewModelScope.async {
        val weeklyData = getWeeklyAsync().await()
        var amount = 0
        for (data in weeklyData)
            amount += data.amount
        amount
    }

    fun getMonthlyCountAsync() = viewModelScope.async {
        val monthlyData = getMonthlyAsync().await()
        var amount = 0
        for (data in monthlyData)
            amount += data.amount
        amount
    }

    fun delete(date: Date) =
        viewModelScope.launch(Dispatchers.IO) { repository.delete(date) }

    fun delete(handwashing: Handwashing) =
        viewModelScope.launch(Dispatchers.IO) { repository.delete(handwashing) }
}