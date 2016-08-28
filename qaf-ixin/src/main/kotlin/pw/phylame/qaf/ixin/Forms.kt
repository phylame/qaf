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
import javax.swing.Action
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.JToolBar

open class Form(title: String = "", val snap: Settings? = null) : JFrame(title) {
    val actions = HashMap<String, Action>()

    var toolBar: JToolBar? = null

    var statusBar: StatusBar? = null

    var statusText: String get() = statusBar?.text ?: ""
        set(value) {
            statusBar?.text = value
        }

    init {
        init()
    }

    protected fun createComponents(designer: Designer, listener: CommandListener? = null) {
        val delegate = Ixin.myDelegate
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

    open fun init() {
        restoreStatus()
    }

    open fun destroy() {
        saveStatus()
    }

    open protected fun restoreStatus() {
        if (snap == null) {
            return
        }
        toolBar?.isVisible = snap[TOOL_BAR_VISIBLE] ?: true
        toolBar?.isLocked = snap[TOOL_BAR_LOCKED] ?: false
        toolBar?.isTextHidden = snap[TOOL_BAR_TEXT_HIDDEN] ?: true
        statusBar?.isVisible = snap[STATUS_BAR_VISIBLE] ?: true
        val point: Point? = snap[FORM_LOCATION] ?: null
        if (point != null) {
            location = point
        }
        size = snap[FORM_DIMENSION] ?: defaultSize
    }

    open protected fun saveStatus() {
        if (snap == null) {
            return
        }
        snap[FORM_LOCATION] = location
        snap[FORM_DIMENSION] = size
        snap[TOOL_BAR_VISIBLE] = toolBar?.isVisible ?: true
        snap[TOOL_BAR_LOCKED] = toolBar?.isLocked ?: false
        snap[TOOL_BAR_TEXT_HIDDEN] = toolBar?.isTextHidden ?: true
        snap[STATUS_BAR_VISIBLE] = statusBar?.isVisible ?: true
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
        statusBar = StatusBar()
    }

    companion object {
        const val FORM_LOCATION = "form.location"
        const val FORM_DIMENSION = "form.dimension"
        const val TOOL_BAR_VISIBLE = "form.toolbar.visible"
        const val TOOL_BAR_LOCKED = "form.toolbar.locked"
        const val TOOL_BAR_TEXT_HIDDEN = "form.toolbar.textHidden"
        const val STATUS_BAR_VISIBLE = "form.statusbar.visible"

        val defaultSize by lazy {
            Toolkit.getDefaultToolkit().screenSize.scaleWith(0.6)
        }
    }
}
