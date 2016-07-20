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

import java.io.File
import java.util.*

interface Settings<R> : Mappable<String, Any> {
    fun getRaw(key: String): R?

    fun <T : Any> parseRaw(raw: R, clazz: Class<T>): T?

    fun reset()

    fun sync(forcing: Boolean = false)

    val isModified: Boolean
}

inline fun <R, reified T : Any> Settings<R>.get(key: String, def: T? = null): T? {
    return get(key) as? T ?: getRaw(key)?.let {
        parseRaw(it, T::class.java)?.apply { set(key, this) }
    } ?: def
}

interface StringRawSettings : Settings<String> {
    override fun <T : Any> parseRaw(raw: String, clazz: Class<T>): T? =
            ConverterManager.parse(raw, clazz)
}

open class PropertiesSettings
private constructor(private val path: String, loading: Boolean, autoSync: Boolean) :
        MappingWrapper<String, Any>(LinkedHashMap()), StringRawSettings {
    companion object {
        var encoding = "UTF-8"

        var commentLabel = "#"

        var valueSeparator = "="

        var lineSeparator = System.lineSeparator()

        var fileSuffix = ".pref"

        fun transformPath(path: String): String =
                if ('.' !in File(path).name) path + fileSuffix else path
    }

    var comment = ""

    private var modified = false

    private val strings = LinkedHashMap<String, String>()

    init {
        if (loading) {
            load()
        }
        modified = false
        if (autoSync) {
            Qaf.registerExitHook(Runnable { sync() })
        }
    }

    override fun getRaw(key: String): String? = strings[key]

    override fun contains(key: String): Boolean = key in strings || super.contains(key)

    protected fun load() {
        val file = File(path)
        if (!file.exists()) {
            reset()
        } else {
            file.inputStream().buffered().use { input ->
                Properties().apply {
                    load(input)
                }.forEach { k, v -> strings[k.toString()] = v.toString() }
            }
        }
    }

    override fun reset() {
        modified = true
    }

    override fun sync(forcing: Boolean) {
        if (isModified || forcing) {
            sync0()
        }
    }

    private fun sync0() {

    }

    override val isModified: Boolean = modified
}
