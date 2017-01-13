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

import pw.phylame.ycl.function.Provider
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

fun mnemonicLabel(text: String): JLabel {
    val label = JLabel()
    label.title = text
    return label
}

fun <T : Component> T.performOn(form: IForm, text: String): T = performOn(form, Provider { text })

fun <T : Component> T.performOn(form: IForm, action: Action): T = performOn(form, Provider {
    action[Action.LONG_DESCRIPTION] ?: ""
})

fun <T : Component> T.performOn(form: IForm, provider: Provider<String>): T {
    addMouseListener(StatusPerformer(provider, form))
    return this
}

fun Component.labelled(text: String): JLabel {
    val label = mnemonicLabel(text)
    label.labelFor = this
    return label
}

val Component?.window: Window get() = if (this == null) {
    JOptionPane.getRootFrame()
} else if (this is Frame || this is Dialog) {
    this as Window
} else {
    parent.window
}

var JLabel.title: String get() = text
    set(value) {
        val result = Ixin.mnemonicOf(value)
        text = result.name
        if (result.isEnable) {
            displayedMnemonic = result.mnemonic
            displayedMnemonicIndex = result.index
        }
    }

var AbstractButton.title: String get() = text
    set(value) {
        val result = Ixin.mnemonicOf(value)
        text = result.name
        if (result.isEnable) {
            mnemonic = result.mnemonic
            displayedMnemonicIndex = result.index
        }
    }

var JToolBar.isLocked: Boolean get() = !isFloatable
    set(value) {
        isFloatable = !value
    }

var JToolBar.isTextHidden: Boolean get() = components.any { it is AbstractButton && it.hideActionText }
    set(value) {
        components.forEach {
            if (it is AbstractButton) {
                it.hideActionText = value
            }
        }
    }

fun alignedPane(alignment: Int, space: Int, vararg components: Component?): JPanel? = if (components.isEmpty()) {
    null
} else {
    alignedPane(alignment, BoxLayout.LINE_AXIS, space, *components)
}

fun alignedPane(alignment: Int, axis: Int, space: Int, vararg components: Component?): JPanel? {
    if (components.isEmpty()) {
        return null
    }
    val pane = JPanel()
    addAlignedComponents(pane, alignment, axis, space, *components)
    return pane
}

// alignment: -1 for customizing
fun addAlignedComponents(panel: JPanel, alignment: Int, axis: Int, space: Int, vararg components: Component?) {
    if (components.isEmpty()) {
        return
    }

    panel.layout = BoxLayout(panel, axis)
    val hAxis = axis == BoxLayout.LINE_AXIS || axis == BoxLayout.X_AXIS
    if (alignment != -1) {
        if (hAxis) {
            if (alignment != SwingConstants.LEFT) {
                panel.add(Box.createHorizontalGlue())
            }
        } else if (alignment != SwingConstants.TOP) {
            panel.add(Box.createVerticalGlue())
        }
    }

    val rigid = if (hAxis) Dimension(space, 0) else Dimension(0, space)

    val end = components.size - 1
    for (ix in 0..end - 1) {
        panel.add(components[ix])
        panel.add(Box.createRigidArea(rigid))
    }
    panel.add(components[end])

    if (alignment != -1) {
        if (hAxis) {
            if (alignment != SwingConstants.RIGHT) {
                panel.add(Box.createHorizontalGlue())
            }
        } else if (alignment != SwingConstants.BOTTOM) {
            panel.add(Box.createVerticalGlue())
        }
    }
}

fun groupedPane(rows: Int, columns: Int, hSpace: Int, vSpace: Int, vararg components: Component?): JPanel? {
    if (components.isEmpty()) {
        return null
    }
    val pane = JPanel()
    addGroupedComponents(pane, rows, columns, hSpace, vSpace, *components)
    return pane
}

fun addGroupedComponents(panel: JPanel, rows: Int, columns: Int, hSpace: Int, vSpace: Int, vararg components: Component?) {
    if (components.isEmpty()) {
        return
    }

    val layout = GroupLayout(panel)
    panel.layout = layout

    val hGroup = layout.createSequentialGroup()
    var group: GroupLayout.ParallelGroup
    var item: Component?
    var index: Int
    for (column in 0..columns - 1) {
        group = layout.createParallelGroup()
        for (row in 0..rows - 1) {
            index = row * columns + column
            item = components[index]
            if (item != null) {
                group.addComponent(item)
                if (row != rows - 1) {
                    group.addGap(vSpace)
                }
            }
        }
        hGroup.addGroup(group)
        if (column != columns - 1) {
            hGroup.addGap(hSpace)
        }
    }
    layout.setHorizontalGroup(hGroup)

    val vGroup = layout.createSequentialGroup()
    for (row in 0..rows - 1) {
        group = layout.createParallelGroup(GroupLayout.Alignment.CENTER)
        for (column in 0..columns - 1) {
            item = components[row * columns + column]
            if (item != null) {
                group.addComponent(item)
                if (column != columns - 1) {
                    group.addGap(hSpace)
                }
            }
        }
        vGroup.addGroup(group)
        if (row != rows - 1) {
            vGroup.addGap(vSpace)
        }
    }

    layout.setVerticalGroup(vGroup)
}

private class StatusPerformer(val provider: Provider<String>, val form: IForm) : MouseAdapter() {
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
