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

import java.io.*
import java.nio.charset.Charset
import java.util.*

class Settings(name: String, loading: Boolean = true, autoSync: Boolean) {
    companion object {
        val ENCODING = "UTF-8"

        val COMMENT_LABEL = "#"

        val VALUE_SEPARATOR = "="

        val LINE_SEPARATOR = System.lineSeparator()

        val FILE_SUFFIX = ".pref"
    }

    private val settings = LinkedHashMap<String, String>()

    var modified: Boolean = false
        private set

    var comment: String = ""

    val path = App.pathInHome(name + FILE_SUFFIX)

    init {
        if (loading) {
            val file = File(path)
            if (!file.exists()) {
                reset()
            } else {
                FileInputStream(file).use {
                    load(it)
                }
            }
        }
        if (autoSync) {
            App.addCleanup(Runnable {
                sync(false)
            })
        }
    }

    fun reset() {
        modified = true
    }

    fun sync(forcing: Boolean = false) {
        if (modified || forcing) {
            val dir = File(path).parentFile
            if (!dir.exists() && !dir.mkdir()) {
                throw IOException("Cannot create settings home: ${dir.absolutePath}")
            }
            FileOutputStream(path).use {
                store(it)
            }
            modified = false
        }
    }

    private fun load(input: InputStream) {
        input.bufferedReader(Charset.forName(ENCODING)).forEachLine {
            val line = it.trim()
            if (line.isNotEmpty() && !line.startsWith(COMMENT_LABEL)) {
                val pos = line.indexOf(VALUE_SEPARATOR)
                if (pos > -1) {
                    settings[line.substring(0, pos).trim()] = line.substring(pos + VALUE_SEPARATOR.length)
                }
            }
        }
    }

    private fun store(output: OutputStream) {
        output.bufferedWriter(Charset.forName(ENCODING)).apply {
            if (comment.isNotBlank()) {
                append(comment.lineSequence().joinToString(LINE_SEPARATOR, COMMENT_LABEL + ' '))
                append(COMMENT_LABEL).append(" Encoding: ").append(ENCODING).append(LINE_SEPARATOR)
                append(LINE_SEPARATOR)
            }
            for ((k, v) in settings) {
                append(k).append(VALUE_SEPARATOR).append(v).append(LINE_SEPARATOR)
            }
            flush()
        }
    }

    val size: Int = settings.size

    val names: Set<String> = settings.keys

    fun forEach(action: (Map.Entry<String, String>) -> Unit) {
        settings.forEach(action)
    }

    operator fun contains(name: String): Boolean = name in settings

    operator fun get(name: String): Any? = settings[name]

    inline fun <reified T : Any> get(name: String, default: T): T = get(name, default, T::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(name: String, default: T, clazz: Class<T>): T = settings[name]?.let {
        Converters.parse(it, clazz)
    } ?: default

    operator inline fun <reified T : Any> set(name: String, value: T) {
        set(name, value, T::class.java)
    }

    fun <T : Any> set(name: String, value: T, clazz: Class<T>) {
        settings[name] = Converters.render(value, clazz)!!
        modified = true
    }

    fun update(rhs: Settings, clearing: Boolean = false) {
        if (clearing) {
            settings.clear()
        }
        settings.putAll(rhs.settings)
        modified = true
    }

    fun remove(name: String): String? {
        val prev = settings.remove(name)
        modified = true
        return prev
    }

    fun clear() {
        settings.clear()
        modified = true
    }
}
