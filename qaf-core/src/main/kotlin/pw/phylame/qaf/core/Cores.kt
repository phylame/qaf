/*
 * Copyright 2016 Peng Wan <phylame@163.com>
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
    fun get(key: String): String

    fun tr(key: String): String = get(key)

    fun tr(key: String, vararg args: Any): String = format(get(key), args)

    fun format(pattern: String, args: Array<out Any>): String = MessageFormat.format(pattern, *args)
}

data class Assembly(
        val name: String = "",
        val version: String = ""
)

class Translator
private constructor(val bundle: ResourceBundle) : Localizable {
    constructor(path: String,
                locale: Locale = Locale.getDefault(),
                loader: ClassLoader = Thread.currentThread().contextClassLoader) :
    this(ResourceBundle.getBundle(path, locale, loader))

    override fun get(key: String): String = bundle.getString(key)

}
