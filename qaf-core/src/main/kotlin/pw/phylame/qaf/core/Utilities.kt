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

import java.net.URL
import java.util.*

fun <K, V> MutableMap<K, V>.put(pair: Pair<K, V>): V? = put(pair.first, pair.second)

fun <K, V> MutableMap<K, V>.put(entry: Map.Entry<K, V>): V? = put(entry.key, entry.value)

fun <T> T.iif(cond: Boolean, ok: () -> T): T = if (cond) ok() else this

fun fetchLanguages(url: URL): List<String> {
    val tags = LinkedList<String>()
    url.openStream().bufferedReader().forEachLine {
        val line = it.trim()
        if (line.isNotEmpty() && !line.startsWith('#')) {
            tags.add(line)
        }
    }
    return tags
}
