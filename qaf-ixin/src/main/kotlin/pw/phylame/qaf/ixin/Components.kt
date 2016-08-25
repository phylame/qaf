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

import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JToolBar

var JToolBar.isLocked: Boolean get() = !isFloatable
    set(value) {
        isFloatable = !value
    }

class StatusBar : JPanel(BorderLayout()) {
    companion object {
        var border = 2
    }

    val label: JLabel = JLabel()

    var text: String get() = label.text
        set (value) {
            previous = value
            label.text = text
        }

    init {
        label.border = BorderFactory.createEmptyBorder(0, StatusBar.border, 0, 0)
        add(label, BorderLayout.LINE_START)
    }

    fun setTemporary(text: String) {
        label.text = text
    }

    fun reset() {
        label.text = previous
    }

    // previous text
    private var previous: String? = null
}
