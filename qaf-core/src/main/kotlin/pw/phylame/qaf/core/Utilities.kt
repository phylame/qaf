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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass

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

fun Locale.asTag(): String {
    return if (country.isNotEmpty()) language + '-' + country else language
}

fun String.toLocale(): Locale {
    var index = indexOf('-')
    if (index == -1) {
        index = indexOf('_')
    }
    val language: String
    val country: String
    if (index == -1) {
        language = this
        country = ""
    } else {
        language = substring(0, index)
        country = substring(index + 1)
    }
    return Locale(language, country)
}

interface Converter<T> {
    fun render(o: T): String = o.toString()

    fun parse(s: String): T
}

object Converters {
    private val converters = HashMap<Class<*>, Converter<*>>()

    inline fun <reified T : Any> set(converter: Converter<T>): Converter<T>? =
            set(T::class.javaObjectType, converter)

    operator fun <T : Any> set(clazz: KClass<T>, converter: Converter<T>): Converter<T>? =
            set(clazz.javaObjectType, converter)

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> set(clazz: Class<T>, converter: Converter<T>): Converter<T>? =
            converters.put(clazz, converter) as? Converter<T>

    operator fun <T : Any> contains(clazz: Class<T>): Boolean = clazz in converters

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(clazz: Class<T>): Converter<T>? = converters[clazz] as? Converter<T>

    fun <T : Any> render(o: T, clazz: Class<T>): String? = get(clazz)?.render(o)

    inline fun <reified T : Any> render(o: T): String? = render(o, T::class.java)

    fun <T : Any> parse(s: String, clazz: Class<T>): T? = get(clazz)?.parse(s)

    inline fun <reified T : Any> parse(s: String): T? = parse(s, T::class.java)

    init {
        set(object : Converter<String> {
            override fun parse(s: String): String = s
        })

        set(object : Converter <Boolean> {
            override fun parse(s: String): Boolean = s.toBoolean()
        })

        set(object : Converter<Byte> {
            override fun parse(s: String): Byte = s.toByte()
        })

        set(object : Converter<Short> {
            override fun parse(s: String): Short = s.toShort()
        })

        set(object : Converter<Int> {
            override fun parse(s: String): Int = s.toInt()
        })

        set(object : Converter<Long> {
            override fun parse(s: String): Long = s.toLong()
        })

        set(object : Converter<Float> {
            override fun parse(s: String): Float = s.toFloat()
        })

        set(object : Converter<Double> {
            override fun parse(s: String): Double = s.toDouble()
        })

        set(object : Converter<Date> {
            private val format by lazy {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            }

            override fun render(o: Date): String = format.format(o)

            override fun parse(s: String): Date = format.parse(s)
        })

        set(object : Converter<Locale> {
            override fun render(o: Locale): String = o.asTag()
            override fun parse(s: String): Locale = s.toLocale()
        })
    }
}

fun main(args: Array<String>) {
    println(Converters.render(Date()))
    println(Converters.parse<Locale>("en-US"))
    println(Converters.parse<Int>("124"))
}
