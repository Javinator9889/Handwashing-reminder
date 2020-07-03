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
package com.javinator9889.handwashingreminder.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.javinator9889.handwashingreminder.data.room.entities.Handwashing
import java.util.*

@Dao
interface HandwashingDao {
    @Query("SELECT * FROM handwashing ORDER BY date ASC")
    fun getAll(): LiveData<List<Handwashing>>

    @Query("SELECT * FROM handwashing WHERE date BETWEEN :from AND :to")
    suspend fun getBetween(from: Date, to: Date): List<Handwashing>

    @Query("SELECT * FROM handwashing WHERE date == :date")
    suspend fun get(date: Date): Handwashing?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun create(handwashing: Handwashing)

    @Update
    suspend fun update(handwashing: Handwashing)

    @Query("UPDATE handwashing SET amount = amount + 1 WHERE date == :date")
    suspend fun increment(date: Date)

    @Query("UPDATE handwashing SET amount = CASE WHEN (amount == 0) THEN 0 ELSE amount - 1 END WHERE date == :date")
    suspend fun decrement(date: Date)

    @Query("DELETE FROM handwashing WHERE date == :date")
    suspend fun delete(date: Date)

    @Delete
    suspend fun delete(handwashing: Handwashing)
}