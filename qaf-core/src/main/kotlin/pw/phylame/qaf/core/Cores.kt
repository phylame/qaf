/*
 * Copyright 2015-2016 Peng Wan <phylame@163.com>
 *
 * This file is part of Qaf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.qaf.core

import pw.phylame.commons.util.MiscUtils
import pw.phylame.commons.util.StringUtils
import java.text.MessageFormat
import java.util.*

data class Assembly(val name: String = "", val version: String = "")

interface Localizable {
    fun get(key: String): String

    fun tr(key: String): String = if (key.startsWith('!')) key.substring(1) else get(key)

    fun optTr(key: String, default: String): String = try {
        StringUtils.notEmptyOr(tr(key), default)
    } catch (e: MissingResourceException) {
        default
    }

    fun tr(key: String, vararg args: Any?): String = format(if (key.startsWith('!')) key.substring(1) else tr(key), args)

    fun optTr(key: String, default: String, vararg args: Any?): String = format(optTr(key, default), args)

    fun format(pattern: String, args: Array<out Any?>): String = MessageFormat.format(pattern, *args)
}

open class LocalizableWrapper : Localizable {
    lateinit var translator: Localizable

    override fun get(key: String): String = translator.get(key)

    override fun tr(key: String): String = translator.tr(key)

    override fun optTr(key: String, default: String): String = translator.optTr(key, default)

    override fun tr(key: String, vararg args: Any?): String = translator.tr(key, *args)

    override fun optTr(key: String, default: String, vararg args: Any?): String = translator.optTr(key, default, *args)

    override fun format(pattern: String, args: Array<out Any?>): String = translator.format(pattern, args)
}

class Translator private constructor(val bundle: ResourceBundle) : Localizable {
    constructor(path: String, locale: Locale = Locale.getDefault(), loader: ClassLoader = MiscUtils.getContextClassLoader()) :
            this(ResourceBundle.getBundle(path, locale, loader))

    override fun get(key: String): String = bundle.getString(key)
}
