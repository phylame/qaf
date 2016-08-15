/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
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

interface Localizable {
    fun tr(key: String, default: String = ""): String = get(key) ?: default

    fun tr(key: String, vararg args: Any, default: String = ""): String = format(get(key) ?: default, args)

    fun format(pattern: String, args: Array<out Any>): String = MessageFormat.format(pattern, args)

    fun get(key: String): String?
}

class Assembly(
        val name: String,
        val version: String
)

class Translator
private constructor(val bundle: ResourceBundle) : Localizable {
    constructor(path: String,
                locale: Locale = Locale.getDefault(),
                loader: ClassLoader = Thread.currentThread().contextClassLoader) : this(ResourceBundle.getBundle(path, locale, loader))

    override fun get(key: String): String? =
            try {
                bundle.getString(key)
            } catch (e: MissingResourceException) {
                null
            }
}

interface Mappable<K, V> {
    operator fun set(key: K, value: V)

    fun put(pair: Pair<K, V>) {
        this[pair.first] = pair.second
    }

    fun put(entry: Map.Entry<K, V>) {
        this[entry.key] = entry.value
    }

    fun update(map: Map<K, V>) {
        map.forEach { put(it) }
    }

    fun update(m: Mappable<K, V>) {
        update(m.items)
    }

    operator fun contains(key: K): Boolean

    operator fun get(key: K): V?

    val items: Map<K, V>

    fun remove(key: K): V?

    fun clear()
}

open class MappingWrapper<K, V>(private val m: MutableMap<K, V>) : Mappable<K, V> {
    override fun set(key: K, value: V) {
        m[key] = value
    }

    override fun contains(key: K): Boolean = key in m

    override fun get(key: K): V? = m[key]

    override val items: Map<K, V> = m

    override fun remove(key: K): V? = m.remove(key)

    override fun clear() {
        m.clear()
    }
}
