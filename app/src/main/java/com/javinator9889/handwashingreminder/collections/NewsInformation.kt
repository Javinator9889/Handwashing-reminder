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
 * Created by Javinator9889 on 3/06/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.collections

import com.beust.klaxon.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KProperty

data class NewsData(
    val id: String,
    @Json(name = "discoverDate")
    @KlaxonDate
    val date: Date,
    val title: String,
    val text: String,
    val url: String,
    @Json(name = "elements")
    @KlaxonElements
    val imageUrl: String? = null,
    val website: Website? = null
)

data class Elements(val url: String?)

data class Website(
    val name: String,
    val hostName: String,
    val iconURL: String?
)

@Target(AnnotationTarget.FIELD)
annotation class KlaxonDate

val dateConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == Date::class.java

    override fun fromJson(jv: JsonValue) =
        if (jv.string != null) {
            val format =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
            format.parse(jv.string!!)
        } else throw KlaxonException("Couldn't parse date: ${jv.string}")

    override fun toJson(value: Any) = """ { "date": $value } """
}

@Target(AnnotationTarget.FIELD)
annotation class KlaxonElements

val elementConverter = object : Converter {
    override fun canConvert(cls: Class<*>) = cls == String::class.java

    override fun fromJson(jv: JsonValue) =
        if (jv.array != null) {
            val elements = jv.array!!
            try {
                (elements[0] as Elements).url
            } catch (e: Exception) {
                Timber.w(e, "Captured exception while parsing Klaxon value")
                null
            }
        } else throw KlaxonException("Couldn't parse array ${jv.array}")

    override fun toJson(value: Any) = """ { "imageUrl": "$value" } """
}

val newsStrategy = object : PropertyStrategy {
    private val acceptedProperties =
        setOf(
            "id",
            "publishDate",
            "title",
            "text",
            "url",
            "elements",
            "website",
            "url",
            "name",
            "hostName",
            "iconURL"
        )

    override fun accept(property: KProperty<*>) =
        property.name in acceptedProperties
}