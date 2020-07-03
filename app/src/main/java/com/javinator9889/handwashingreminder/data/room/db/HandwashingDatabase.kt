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
package com.javinator9889.handwashingreminder.data.room.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.javinator9889.handwashingreminder.data.room.dao.HandwashingDao
import com.javinator9889.handwashingreminder.data.room.entities.Handwashing
import com.javinator9889.handwashingreminder.utils.room.Converters

@Database(entities = [Handwashing::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HandwashingDatabase : RoomDatabase() {
    abstract fun handwashingDao(): HandwashingDao

    companion object {
        @Volatile
        private var instance: HandwashingDatabase? = null

        fun getDatabase(context: Context): HandwashingDatabase {
            instance?.let { return it }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HandwashingDatabase::class.java,
                    "handwashing_db"
                ).build()
                this.instance = instance
                return instance
            }
        }
    }
}