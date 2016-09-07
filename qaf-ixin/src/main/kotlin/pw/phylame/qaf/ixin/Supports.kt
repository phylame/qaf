/*
 * Copyright 2015-2016 Peng Wan <phylame@163.com>
 *
 * This file is part of IxIn.
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

import pw.phylame.qaf.core.App
import pw.phylame.ycl.format.Converter
import pw.phylame.ycl.format.Converters
import pw.phylame.ycl.io.IOUtils
import pw.phylame.ycl.util.StringUtils
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Point
import java.util.*
import javax.swing.*

fun Dimension.scaleWith(rate: Double): Dimension = Dimension((width * rate).toInt(), (height * rate).toInt())

object Ixin {
    const val DEFAULT_THEME_NAME = "Default"
    const val FONT_KEY_PATH = "!pw/phylame/qaf/ixin/font-keys.txt"
    const val MNEMONIC_PREFIX = '&'

    var mnemonicEnable = true

    val isMnemonicSupport by lazy {
        "mac" !in System.getProperty("os.name")
    }

    val themes = HashMap<String, String>()

    fun themeFor(name: String): String = themes[name] ?: name

    init {
        for (feel in UIManager.getInstalledLookAndFeels()) {
            themes[feel.name] = feel.className
        }
    }

    @Suppress("unchecked_cast")
    val myDelegate: IxinDelegate<*> get() = if (App.delegate is IxinDelegate<*>)
        App.delegate as IxinDelegate<*>
    else throw IllegalStateException("App should run with IxinDelegate")

    // 1
    fun setAntiAliasing(enable: Boolean) {
        System.setProperty("awt.useSystemAAFontSettings", if (enable) "on" else "off")
        System.setProperty("swing.aatext", enable.toString())
    }

    // 2
    fun setWindowDecorated(decorated: Boolean) {
        JDialog.setDefaultLookAndFeelDecorated(decorated)
        JFrame.setDefaultLookAndFeelDecorated(decorated)
    }

    // 3
    fun setLafTheme(name: String) {
        if (name.isNotEmpty() && name != DEFAULT_THEME_NAME) {
            try {
                UIManager.setLookAndFeel(themeFor(name))
            } catch (e: Exception) {
                throw RuntimeException("cannot set to new laf: $name", e)
            }
        }
    }

    val fontKeys by lazy {
        val keys = LinkedList<String>()
        IOUtils.openResource(FONT_KEY_PATH, Ixin::class.java.classLoader)?.bufferedReader()?.forEachLine {
            keys.add(it.trim())
        }
        keys
    }

    // 4
    fun setGlobalFont(font: Font) {
        val defaults = UIManager.getLookAndFeelDefaults()
        for (key in fontKeys) {
            val value = defaults[key]
            if (value == null) {
                defaults[key] = font
            } else if (value is Font) {
                defaults[key] = font.deriveFont(value.style)
            } else if (value is UIDefaults.ActiveValue) {
                defaults[key] = font.deriveFont((value.createValue(defaults) as Font).style)
            } else if (value is UIDefaults.LazyValue) {
                defaults[key] = font.deriveFont((value.createValue(defaults) as Font).style)
            }
        }
    }

    data class MnemonicTuple(val name: String, val mnemonic: Int, val index: Int) {
        val isEnable: Boolean get() = mnemonicEnable && mnemonic != 0
    }

    fun mnemonicOf(name: String): MnemonicTuple {
        // get mnemonic from name
        var text = name
        var mnemonic = 0

        val index = name.indexOf(MNEMONIC_PREFIX)
        if (index >= 0 && index < name.length) {
            val next = name[index + 1]
            if (next.isLetterOrDigit()) {     // has mnemonic
                mnemonic = next.toInt()
                text = name.substring(0, index) + name.substring(index + 1)
            }
        }
        return MnemonicTuple(text, mnemonic, index)
    }

    fun trimMnemonic(text: String, mnemonicIndex: Int, bracketLength: Int = 1): String {
        if (mnemonicIndex == -1 || bracketLength == 0) {
            return text
        }
        return text.substring(0, mnemonicIndex - bracketLength) + text.substring(mnemonicIndex + 1 + bracketLength)
    }

    fun formatKeyStroke(keyStroke: KeyStroke): String {
        var str = keyStroke.toString()
        str = str.replaceFirst("ctrl ", "Ctrl+")
        str = str.replaceFirst("shift ", "Shift+")
        str = str.replaceFirst("alt ", "Alt+")
        str = str.replaceFirst("typed ", "")
        str = str.replaceFirst("pressed ", "")
        str = str.replaceFirst("released ", "")
        return str
    }


    init {
        // register converter for UI objects
        Converters.set(Point::class.java, object : Converter<Point> {
            override fun parse(str: String): Point {
                val pair = StringUtils.partition(str, '-')
                return Point(Integer.decode(pair.first.trim()), Integer.decode(pair.second.trim()))
            }

            override fun render(o: Point): String = "${o.x}-${o.y}"
        })

        Converters.set(Dimension::class.java, object : Converter<Dimension> {
            override fun parse(str: String): Dimension {
                val pair = StringUtils.partition(str, '-')
                return Dimension(Integer.decode(pair.first.trim()), Integer.decode(pair.second.trim()))
            }

            override fun render(o: Dimension): String = "${o.width}-${o.height}"
        })

        Converters.set(Font::class.java, object : Converter<Font> {
            override fun parse(str: String): Font = Font.decode(str)

            override fun render(o: Font): String {
                val b = StringBuilder(o.family).append('-')
                when (o.style) {
                    Font.PLAIN -> b.append("plain")
                    Font.BOLD -> b.append("bold")
                    Font.ITALIC -> b.append("italic")
                    Font.BOLD or Font.ITALIC -> b.append("bolditalic")
                }
                return b.append('-').append(o.size).toString()
            }
        })

        Converters.set(Color::class.java, object : Converter<Color> {
            override fun parse(str: String): Color = Color.decode(str)

            override fun render(o: Color): String = "#%X".format(o.rgb).substring(2)
        })
    }
}
