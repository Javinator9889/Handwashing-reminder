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

import com.beust.klaxon.PropertyStrategy
//import java.util.*
import kotlin.reflect.KProperty

data class NewsData(
    val id: String,
    val publishDate: String,
    val title: String,
    val language: String,
    val text: String,
    val url: String,
    val elements: List<Elements>,
    val website: Website?
)

data class Elements(
    val type: String,
    val url: String?
)

data class Website(
    val name: String,
    val hostName: String,
    val iconURL: String?
)

val newsStrategy = object : PropertyStrategy {
    private val acceptedProperties =
        setOf(
            "id",
            "publishDate",
            "title",
            "language",
            "text",
            "url",
            "elements",
            "website",
            "type",
            "url",
            "name",
            "hostName",
            "iconURL"
        )

    override fun accept(property: KProperty<*>) =
        property.name in acceptedProperties
}