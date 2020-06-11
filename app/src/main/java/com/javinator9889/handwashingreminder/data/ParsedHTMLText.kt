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
 * Created by Javinator9889 on 11/06/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.data

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

data class ParsedHTMLText(
    val name: CharSequence,
    val shortDescription: CharSequence,
    val longDescription: CharSequence,
    val provider: CharSequence,
    val website: CharSequence,
    val symptoms: CharSequence,
    val prevention: CharSequence
) : Parcelable {
    constructor(parcel: Parcel) : this(
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        TextUtils.writeToParcel(name, parcel, flags)
        TextUtils.writeToParcel(shortDescription, parcel, flags)
        TextUtils.writeToParcel(longDescription, parcel, flags)
        TextUtils.writeToParcel(provider, parcel, flags)
        TextUtils.writeToParcel(website, parcel, flags)
        TextUtils.writeToParcel(symptoms, parcel, flags)
        TextUtils.writeToParcel(prevention, parcel, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<ParsedHTMLText> {
        override fun createFromParcel(parcel: Parcel) = ParsedHTMLText(parcel)

        override fun newArray(size: Int) = arrayOfNulls<ParsedHTMLText>(size)
    }
}