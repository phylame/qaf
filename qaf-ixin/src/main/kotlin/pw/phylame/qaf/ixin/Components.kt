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

import pw.phylame.ycl.util.Provider
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

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
            label.text = value
        }

    init {
        label.border = BorderFactory.createEmptyBorder(0, StatusBar.border, 0, 0)
        add(label, BorderLayout.LINE_START)
    }

    fun perform(text: String) {
        label.text = text
    }

    fun reset() {
        label.text = previous
    }

    // previous text
    private var previous: String? = null
}

fun mnemonicLabel(text: String): JLabel {
    val result = Ixin.mnemonicOf(text)
    val label = JLabel(result.name)
    if (result.isEnable) {
        label.displayedMnemonic = result.mnemonic
        label.displayedMnemonicIndex = result.index
    }
    return label
}

var <T : JToolBar> T.isTextHidden: Boolean get() = components.any { it is AbstractButton && it.hideActionText }
    set(value) {
        components.forEach {
            if (it is AbstractButton) {
                it.hideActionText = value
            }
        }
    }

fun Component.withLabel(text: String): JLabel {
    val label = mnemonicLabel(text)
    label.labelFor = this
    return label
}

fun alignedPane(alignment: Int, space: Int, vararg components: Component): JPanel? {
    if (components.isEmpty()) {
        return null
    }

    val pane = JPanel()
    pane.layout = BoxLayout(pane, BoxLayout.LINE_AXIS)

    if (alignment != -1 && alignment != SwingConstants.LEFT) {
        pane.add(Box.createHorizontalGlue())
    }

    val end = components.size - 1
    for (ix in 0..end - 1) {
        pane.add(components[ix])
        pane.add(Box.createHorizontalStrut(space))
    }
    pane.add(components[end])

    if (alignment != -1 && alignment != SwingConstants.RIGHT) {
        pane.add(Box.createHorizontalGlue())
    }
    return pane
}

fun <T : Component> T.performOn(form: Form, provider: Provider<String>): T {
    addMouseListener(StatusPerformer(provider, form))
    return this
}

fun <T : Component> T.performOn(form: Form, text: String): T = performOn(form, Provider { text })

fun <T : Component> T.performOn(form: Form, action: Action): T = performOn(form, Provider {
    action[Action.LONG_DESCRIPTION] ?: ""
})

private class StatusPerformer(val provider: Provider<String>, val form: Form) : MouseAdapter() {
    private var closed = true

    override fun mouseEntered(e: MouseEvent) {
        showTip()
    }

    override fun mouseExited(e: MouseEvent) {
        closeTip()
    }

    override fun mouseReleased(e: MouseEvent) {
        closeTip()
    }

    private fun showTip() {
        val text = provider.provide()
        if (text.isNotEmpty()) {
            form.statusBar?.perform(text)
            closed = false
        }
    }

    private fun closeTip() {
        if (!closed) {
            form.statusBar?.reset()
            closed = true
        }
    }
}
