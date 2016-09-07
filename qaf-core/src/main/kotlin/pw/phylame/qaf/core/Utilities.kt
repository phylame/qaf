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

import java.net.URL
import java.nio.charset.Charset
import java.util.*
import kotlin.reflect.KProperty

fun <K, V> MutableMap<K, V>.put(pair: Pair<K, V>): V? = put(pair.first, pair.second)

fun <K, V> MutableMap<K, V>.put(entry: Map.Entry<K, V>): V? = put(entry.key, entry.value)

fun <T> T.iif(cond: Boolean, ok: (T) -> T): T = if (cond) ok(this) else this

class Delegate<out T>(val m: Map<String, *>, val name: String? = null, val fallback: () -> T) {
    @Suppress("unchecked_cast")
    operator fun getValue(ref: kotlin.Any?, property: KProperty<*>): T = m[name ?: property.name] as? T ?: fallback()
}

fun <T> valueOf(m: Map<String, Any>, name: String? = null, fallback: () -> T): Delegate<T> = Delegate(m, name, fallback)

fun URL.lines(charset: Charset = Charsets.UTF_8): Sequence<String> = openStream().bufferedReader(charset).use {
    it.lineSequence().filter {
        val line = it.trim()
        line.isNotEmpty() && line.startsWith("#")
    }
}
