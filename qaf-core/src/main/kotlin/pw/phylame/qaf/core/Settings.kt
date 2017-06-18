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

import pw.phylame.commons.text.Converters
import pw.phylame.commons.util.DateUtils
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.reflect.KProperty

open class Settings(name: String = "settings", loading: Boolean = true, autosync: Boolean = true) {
    companion object {
        var encoding = "UTF-8"

        var commentLabel = "#"

        var valueSeparator = "="

        var fileExtension = ".pref"

        var lineSeparator: String = System.lineSeparator()
    }

    private val settings = LinkedHashMap<String, String>()

    var isModified: Boolean = false
        private set

    var comment: String = ""

    val path = App.pathOf(name or "settings" + fileExtension)

    init {
        if (loading) {
            val file = File(path)
            if (file.exists()) {
                FileInputStream(file).use { load(it) }
            }
        }
        if (autosync) {
            App.cleanups.add(Runnable { sync(false) })
        }
    }

    /**
     * The sub implementation cannot use its var and val for super init is not completed.
     */
    open fun reset() {
        isModified = true
    }

    fun sync(forcing: Boolean = false) {
        val file = File(path)
        if (!file.exists()) {
            reset()
            isModified = true
        }
        if (isModified || forcing) {
            val dir = file.parentFile
            if (!dir.exists() && !dir.mkdir()) {
                throw IOException("Cannot create settings home: ${dir.absolutePath}")
            }
            FileOutputStream(file).use { store(it) }
            isModified = false
        }
    }

    protected open fun load(input: InputStream) {
        input.bufferedReader(Charset.forName(encoding))
                .lineSequence()
                .map(String::trim)
                .filter { it.isNotEmpty() && !it.startsWith(commentLabel) }
                .forEach {
                    val pos = it.indexOf(valueSeparator)
                    if (pos > -1) {
                        settings[it.substring(0, pos).trim()] = it.substring(pos + valueSeparator.length)
                    }
                }
    }

    protected open fun store(output: OutputStream) {
        output.bufferedWriter(Charset.forName(encoding)).apply {
            if (comment.isNotBlank()) {
                append(comment.lineSequence().joinToString(lineSeparator) { commentLabel + ' ' + it }).append(lineSeparator)
            }
            append(commentLabel).append(" Last updated: ").append(DateUtils.toISO(Date())).append(lineSeparator)
            append(commentLabel).append(" Encoding: ").append(encoding).append(lineSeparator)
            append(lineSeparator)
            for ((k, v) in settings) {
                append(k).append(valueSeparator).append(v).append(lineSeparator)
            }
            flush()
        }
    }

    val size: Int get() = settings.size

    val names: Set<String> get() = settings.keys

    val items: Set<Map.Entry<String, String>> get() = settings.entries

    fun forEach(action: (Map.Entry<String, String>) -> Unit) {
        settings.forEach(action)
    }

    operator fun contains(name: String): Boolean = name in settings

    fun rawFor(name: String): String? = settings[name]

    inline operator fun <reified T : Any> get(name: String): T? = get(name, null as T?, T::class.java)

    fun <T : Any> get(name: String, default: T?, type: Class<T>): T? = settings[name]?.let {
        Converters.parse(it, type)
    } ?: default

    inline operator fun <reified T : Any> set(name: String, value: T) {
        set(name, value, T::class.java)
    }

    fun <T : Any> set(name: String, value: T, type: Class<T>) {
        settings[name] = Converters.render(value, type) ?: throw IllegalArgumentException("Unsupported value type: $type")
        isModified = true
    }

    fun update(rhs: Settings, clearing: Boolean = false) {
        update(rhs.settings, clearing)
    }

    fun update(map: Map<String, String>, clearing: Boolean = false) {
        if (clearing) {
            clear()
        }
        settings.putAll(map)
        isModified = true
    }

    fun remove(name: String): String? {
        val prev = settings.remove(name)
        isModified = true
        return prev
    }

    fun clear() {
        settings.clear()
        isModified = true
    }

    inline fun <reified T : Any> delegated(default: T, name: String? = null): Delegate<T> = Delegate(default, T::class.java, name)

    inner class Delegate<T : Any>(val default: T, val type: Class<T>, val name: String? = null) {

        operator fun getValue(ref: Any?, property: KProperty<*>): T = get(name ?: property.name, default, type)!!

        operator fun setValue(ref: Any?, property: KProperty<*>, value: T) {
            set(name ?: property.name, value, type)
        }
    }
}
