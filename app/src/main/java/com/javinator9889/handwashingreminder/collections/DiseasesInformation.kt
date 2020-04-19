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
 * Created by Javinator9889 on 19/04/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.collections

import android.os.Parcel
import android.os.Parcelable
import com.beust.klaxon.Json

class DiseasesListWrapper(val diseases: List<DiseasesInformation>?) :
    Parcelable {
    @Suppress("unchecked_cast")
    constructor(parcel: Parcel) : this(
        parcel.readArrayList(null) as List<DiseasesInformation>?
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(diseases)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DiseasesListWrapper> {
        override fun createFromParcel(parcel: Parcel): DiseasesListWrapper {
            return DiseasesListWrapper(parcel)
        }

        override fun newArray(size: Int): Array<DiseasesListWrapper?> {
            return arrayOfNulls(size)
        }
    }

}

data class DiseasesList(
    var diseases: List<DiseasesInformation>
)

data class DiseasesInformation(
    val name: String,
    @Json(name = "short_description")
    val shortDescription: String,
    @Json(name = "long_description")
    val longDescription: String,
    val provider: String,
    val website: String,
    val symptoms: String,
    val prevention: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(shortDescription)
        parcel.writeString(longDescription)
        parcel.writeString(provider)
        parcel.writeString(website)
        parcel.writeString(symptoms)
        parcel.writeString(prevention)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DiseasesInformation> {
        override fun createFromParcel(parcel: Parcel): DiseasesInformation {
            return DiseasesInformation(parcel)
        }

        override fun newArray(size: Int): Array<DiseasesInformation?> {
            return arrayOfNulls(size)
        }
    }
}
