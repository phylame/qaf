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

import java.text.MessageFormat
import java.util.*
import kotlin.reflect.KProperty

infix fun <T : CharSequence> T?.or(default: T): T = if (isNullOrEmpty()) default else this!!

data class Assembly(val name: String, val version: String, val description: String = "")

open class MapGetter<out T>(val m: Map<String, *>, val name: String = "", val default: () -> T) {
    fun keyOf(property: KProperty<*>) = if (name.isNotEmpty()) name else property.name

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(ref: Any, property: KProperty<*>): T = m[keyOf(property)] as? T ?: default()
}

interface Localizable {
    fun get(key: String): String

    fun tr(key: String): String = get(key)

    fun tr(key: String, vararg args: Any?): String = format(tr(key), args)

    fun optTr(key: String, default: String): String = try {
        get(key) or default
    } catch (e: Exception) {
        default
    }

    fun optTr(key: String, default: String, vararg args: Any?): String = format(optTr(key, default), args)

    fun format(pattern: String, args: Array<out Any?>): String = MessageFormat.format(pattern, *args)
}

open class LocalizableWrapper : Localizable {
    lateinit var translator: Localizable

    override fun get(key: String): String = translator.get(key)

    override fun tr(key: String): String = translator.tr(key)

    override fun tr(key: String, vararg args: Any?): String = translator.tr(key, *args)

    override fun optTr(key: String, default: String): String = translator.optTr(key, default)

    override fun optTr(key: String, default: String, vararg args: Any?): String = translator.optTr(key, default, *args)

    override fun format(pattern: String, args: Array<out Any?>): String = translator.format(pattern, args)
}

class Translator private constructor(val bundle: ResourceBundle) : Localizable {
    constructor(path: String) : this(ResourceBundle.getBundle(path))

    constructor(path: String, locale: Locale) : this(ResourceBundle.getBundle(path, locale))

    constructor(path: String, locale: Locale, loader: ClassLoader) : this(ResourceBundle.getBundle(path, locale, loader))

    override fun get(key: String): String = bundle.getString(key)
}

operator fun Localizable.get(key: String): String = tr(key)
