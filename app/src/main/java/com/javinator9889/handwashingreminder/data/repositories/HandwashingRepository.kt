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
package com.javinator9889.handwashingreminder.data.repositories

import androidx.lifecycle.LiveData
import com.javinator9889.handwashingreminder.data.room.dao.HandwashingDao
import com.javinator9889.handwashingreminder.data.room.entities.Handwashing
import java.util.*

class HandwashingRepository(private val dao: HandwashingDao) {
    val allData: LiveData<List<Handwashing>> = dao.getAll()

    suspend fun create(handwashing: Handwashing) = dao.create(handwashing)
    suspend fun increment(date: Date) = dao.increment(date)
    suspend fun decrement(date: Date) = dao.decrement(date)
    suspend fun update(handwashing: Handwashing) = dao.update(handwashing)
    suspend fun get(date: Date) = dao.get(date)
    suspend fun getBetween(from: Date, to: Date) = dao.getBetween(from, to)
    suspend fun delete(date: Date) = dao.delete(date)
    suspend fun delete(handwashing: Handwashing) = dao.delete(handwashing)
}