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

import pw.phylame.ycl.util.Exceptions
import kotlin.reflect.KProperty

fun <T> T.iif(cond: Boolean, ok: (T) -> T): T = if (cond) ok(this) else this

fun Throwable.dump(): String = Exceptions.dumpToString(this)


open class MapGetter<out T>(val m: Map<String, *>, val name: String = "", val fallback: () -> T) {
    fun keyOf(property: KProperty<*>) = if (name.isNotEmpty()) name else property.name

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any, property: KProperty<*>): T = m[keyOf(property)] as? T ?: fallback()
}
