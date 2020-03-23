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
 * Created by Javinator9889 on 18/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.views.custom.timeconfig

import android.os.Parcel
import android.os.Parcelable

class TimeConfigContent(val title: String, val id: Long) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!, parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeLong(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TimeConfigContent> {
        override fun createFromParcel(parcel: Parcel): TimeConfigContent {
            return TimeConfigContent(
                parcel
            )
        }

        override fun newArray(size: Int): Array<TimeConfigContent?> {
            return arrayOfNulls(size)
        }
    }
}