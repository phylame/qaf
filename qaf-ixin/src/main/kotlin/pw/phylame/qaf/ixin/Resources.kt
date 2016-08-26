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

package pw.phylame.qaf.ixin

import pw.phylame.qaf.core.Translator
import pw.phylame.qaf.core.iif
import pw.phylame.ycl.io.IOUtils
import java.awt.Image
import java.awt.Toolkit
import java.net.URL
import java.util.*
import javax.swing.Icon
import javax.swing.ImageIcon

class Resource(dir: String,
               val gfxDir: String = "gfx",
               val i18nDir: String = "i18n",
               val loader: ClassLoader = Resource::class.java.classLoader) {

    val baseDir = if (dir.endsWith('/')) dir else dir + '/'

    fun iconFor(name: String, suffix: String = ""): Icon? {
        val path = gfxDir + '/' + normalize(name, suffix)
        var icon = icons[path]
        if (icon != null) {
            return icon
        }
        val url = itemFor(path)
        if (url != null) {
            icon = ImageIcon(url)
            icons[path] = icon
        }
        return icon
    }

    fun imageFor(name: String, suffix: String = ""): Image? {
        val path = gfxDir + '/' + normalize(name, suffix)
        var image = images[path]
        if (image != null) {
            return image
        }
        val url = itemFor(path)
        if (url != null) {
            image = Toolkit.getDefaultToolkit().getImage(url)
            images[path] = image
        }
        return image
    }

    fun translatorFor(name: String, locale: Locale = Locale.getDefault()): Translator =
            Translator((if (baseDir.startsWith(IOUtils.CLASS_PATH_PREFIX)) baseDir.substring(1) else baseDir)
                    + i18nDir + '/' + name, locale, loader)

    fun itemFor(name: String, suffix: String = ""): URL? = IOUtils.resourceFor(baseDir + normalize(name, suffix), loader)

    fun normalize(name: String, suffix: String = ""): String = name.iif(suffix.isNotEmpty()) {
        val index = name.indexOf('.')
        if (index == -1) name + suffix else name.substring(0, index) + suffix + name.substring(index)
    }

    private val icons = HashMap<String, Icon>()
    private val images = HashMap<String, Image>()
}
