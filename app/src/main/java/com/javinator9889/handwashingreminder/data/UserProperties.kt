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
package com.javinator9889.handwashingreminder.data

import javinator9889.localemanager.utils.languagesupport.LanguagesSupport.Language
import java.util.*


object UserProperties {
    @Language
    private lateinit var privateLanguage: String

    @Language
    val language: String
    get() {
        if (::privateLanguage.isInitialized)
            return privateLanguage
        privateLanguage = when (Locale.getDefault().language) {
            Locale(Language.SPANISH).language -> Language.SPANISH
            else -> Language.ENGLISH
        }
        return privateLanguage
    }
}