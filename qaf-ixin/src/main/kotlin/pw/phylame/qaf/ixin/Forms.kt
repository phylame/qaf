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
import pw.phylame.qaf.core.Localizable
import pw.phylame.qaf.core.Settings
import java.awt.BorderLayout
import java.awt.Point
import java.awt.Toolkit
import java.util.*
import javax.swing.*

class IStatusBar : JPanel(BorderLayout()) {
    companion object {
        var borderSize = 2
    }

    val label: JLabel = JLabel()

    var text: String get() = label.text
        set (value) {
            previous = value
            label.text = value
        }

    init {
        label.border = BorderFactory.createEmptyBorder(0, borderSize, 0, 0)
        add(JSeparator(), BorderLayout.PAGE_START)
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

open class IForm(title: String = "", val snap: Settings? = null) : JFrame(title) {
    val actions = HashMap<String, Action>()

    var toolBar: JToolBar? = null

    var statusBar: IStatusBar? = null

    var statusText: String get() = statusBar?.text ?: ""
        set(value) {
            statusBar?.text = value
        }

    protected fun createComponents(designer: Designer, listener: CommandListener? = null) {
        val delegate = Ixin.delegate
        val _listener = listener ?: delegate
        val translator = App
        val resource = delegate.resource
        if (designer.menus?.isNotEmpty() ?: false) {
            createMenuBar(designer.menus!!, _listener, translator, resource)
            if (designer.toolbar?.isNotEmpty() ?: false) {
                createToolBar(designer.toolbar!!, _listener, translator, resource)
                contentPane.add(toolBar, BorderLayout.PAGE_START)
            }
        }
        createStatusBar()
        contentPane.add(statusBar, BorderLayout.PAGE_END)
    }

    open fun destroy() {
        saveStatus()
    }

    open protected fun restoreStatus() {
        val snap = this.snap ?: return
        val toolbar = toolBar
        if (toolbar != null) {
            toolbar.isVisible = snap[TOOL_BAR_VISIBLE] ?: true
            toolbar.isLocked = snap[TOOL_BAR_LOCKED] ?: false
            toolbar.isTextHidden = snap[TOOL_BAR_TEXT_HIDDEN] ?: true
        }
        statusBar?.isVisible = snap[STATUS_BAR_VISIBLE] ?: true
        val point: Point? = snap[FORM_LOCATION]
        if (point != null) {
            location = point
        }
        size = snap[FORM_DIMENSION] ?: defaultSize
    }

    open protected fun saveStatus() {
        val snap = this.snap ?: return
        snap[FORM_LOCATION] = location
        snap[FORM_DIMENSION] = size
        val toolbar = toolBar
        if (toolbar != null) {
            snap[TOOL_BAR_VISIBLE] = toolbar.isVisible
            snap[TOOL_BAR_LOCKED] = toolbar.isLocked
            snap[TOOL_BAR_TEXT_HIDDEN] = toolbar.isTextHidden
        }
        snap[STATUS_BAR_VISIBLE] = statusBar?.isVisible ?: true
    }

    fun createPopupMenu(items: Array<Item>, label: String = ""): JPopupMenu =
            JPopupMenu(label).addItems(items, actions, Ixin.delegate, form = this)

    operator fun Action.unaryPlus() {
        val cmd: String? = this[Action.ACTION_COMMAND_KEY]
        if (cmd != null) {
            actions[cmd] = this
        }
    }

    private fun createMenuBar(menus: Array<Group>, listener: CommandListener, translator: Localizable, resource: Resource) {
        val menuBar = JMenuBar()
        for (menu in menus) {
            menuBar.add(menu.asMenu(translator, resource).addItems(menu.items, actions, listener, translator, resource, this))
        }
        if (menuBar.menuCount > 0) {
            jMenuBar = menuBar
        }
    }

    private fun createToolBar(items: Array<Item>, listener: CommandListener, translator: Localizable, resource: Resource) {
        toolBar = JToolBar(title)
        toolBar!!.isRollover = true
        toolBar!!.addItems(items, actions, listener, translator, resource, this)
    }

    private fun createStatusBar() {
        statusBar = IStatusBar()
    }

    companion object {
        const val FORM_LOCATION = "form.location"
        const val FORM_DIMENSION = "form.dimension"
        const val TOOL_BAR_VISIBLE = "form.toolbar.visible"
        const val TOOL_BAR_LOCKED = "form.toolbar.locked"
        const val TOOL_BAR_TEXT_HIDDEN = "form.toolbar.textHidden"
        const val STATUS_BAR_VISIBLE = "form.statusbar.visible"

        val defaultSize by lazy {
            Toolkit.getDefaultToolkit().screenSize.scale(0.6)
        }
    }
}

