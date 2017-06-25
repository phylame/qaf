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

package qaf.ixin

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import javax.swing.*
import javax.swing.border.Border

abstract class ICommonDialog : JDialog {
    companion object {
        var borderWidth = 5
        var controlsSpace = 5

        fun createBorder(width: Int = borderWidth): Border = BorderFactory.createEmptyBorder(width, width, width, width)

        fun <T : Dialog> createDialog(owner: Component?, title: String, modal: Boolean, clazz: Class<T>): T {
            val window = owner.window
            val creator = when (window) {
                is Dialog -> clazz.getConstructor(Dialog::class.java, String::class.java, Boolean::class.java)
                else -> clazz.getConstructor(Frame::class.java, String::class.java, Boolean::class.java)
            }
            return creator.newInstance(window, title, modal)
        }
    }

    protected lateinit var userPane: JPanel
    protected var controlsPane: JPanel? = null
    protected var defaultButton: JButton? = null

    constructor(owner: Frame, title: String, modal: Boolean = false) : super(owner, title, modal)

    constructor(owner: Dialog, title: String, modal: Boolean = false) : super(owner, title, modal)

    fun initialize(resizable: Boolean = true, packing: Boolean = true) {
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                cancelling()
            }
        })
        createContentPane(packing)
        isResizable = resizable
        setLocationRelativeTo(owner)
        if (defaultButton != null) {
            rootPane.defaultButton = defaultButton
        }
    }

    fun setDecorationIfNeed(style: Int) {
        if (isUndecorated) {
            getRootPane().windowDecorationStyle = style
        }
    }

    private fun createContentPane(packing: Boolean) {
        userPane = JPanel(BorderLayout())
        userPane.border = createBorder()
        createComponents(userPane)

        val pane = JPanel(BorderLayout())
        pane.registerKeyboardAction({
            cancelling()
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

        pane.add(userPane, BorderLayout.CENTER)
        if (controlsPane != null && controlsPane!!.componentCount > 0) {
            pane.add(controlsPane, BorderLayout.PAGE_END)
        }

        contentPane = pane
        if (packing) {
            pack()
        }
    }

    protected abstract fun createComponents(userPane: JPanel)

    protected open fun createControlsPane(alignment: Int, vararg components: Component): JPanel? {
        val pane = alignedPane(alignment, controlsSpace, *components)
        if (pane != null) {
            pane.border = createBorder()
        }
        return pane
    }

    protected open fun createCloseButton(id: String): JButton = JButton(object : IAction(id) {
        override fun actionPerformed(e: ActionEvent) {
            cancelling()
        }
    })

    protected open fun cancelling() {
        dispose()
    }
}

// dialog with name returned
abstract class IResultfulDialog<R> : ICommonDialog {
    abstract val result: R?

    constructor(owner: Frame, title: String, modal: Boolean = false) : super(owner, title, modal)

    constructor(owner: Dialog, title: String, modal: Boolean = false) : super(owner, title, modal)

    fun showForResult(resizable: Boolean): R? {
        initialize(resizable)
        isVisible = true
        return result
    }
}

open class IOptionDialog : IResultfulDialog<Int> {
    companion object {
        var minimumSize = Dimension(320, 123)
    }

    var icon: Icon? = null
    lateinit var message: Any
    private var options: Array<out Any>? = null

    // options alignment
    private var alignment: Int = 0

    // index of user selected option
    private var selection: Int = 0

    override val result: Int? get() = selection

    constructor(owner: Frame, title: String, modal: Boolean = false) : super(owner, title, modal)

    constructor(owner: Dialog, title: String, modal: Boolean = false) : super(owner, title, modal)

    fun setOptions(alignment: Int, defaultOption: Int, vararg options: Any) {
        this.alignment = alignment
        this.options = options
        selection = defaultOption
    }

    fun showForOption(resizable: Boolean = false): Int? = showForResult(resizable)

    override fun createComponents(userPane: JPanel) {
        val iconPane = createIconPane()
        if (iconPane != null) {
            userPane.add(iconPane, BorderLayout.LINE_START)
        }
        userPane.add(createMessagePane(), BorderLayout.CENTER)
        controlsPane = createControlsPane()
        minimumSize = Companion.minimumSize
    }

    protected open fun createIconPane(): JPanel? {
        if (icon == null) {
            return null
        }
        val pane = alignedPane(SwingConstants.TOP, BoxLayout.PAGE_AXIS, 0, JLabel(icon))
        if (pane != null) {
            pane.border = createBorder()
        }
        return pane
    }

    private var singleLabel = false

    private fun createMessagePane(): JPanel {
        val pane = JPanel()
        pane.layout = BoxLayout(pane, BoxLayout.PAGE_AXIS)
        pane.border = BorderFactory.createEmptyBorder(borderWidth, borderWidth, 0, borderWidth)
        pane.add(Box.createVerticalGlue())
        singleLabel = icon == null && (message is CharSequence || message is JLabel)
        addMessageComponent(message, pane)
        pane.add(Box.createVerticalGlue())
        return pane
    }

    private fun addMessageComponent(message: Any, messagePane: JPanel) {
        val com: Component
        if (message is CharSequence) {
            com = createTextComponent(message)
        } else if (message is JComponent) {
            message.alignmentX = if (singleLabel) JComponent.CENTER_ALIGNMENT else JComponent.LEFT_ALIGNMENT
            com = message
        } else if (message is Component) {
            com = message
        } else if (message is Array<*>) {
            for (item in message) {
                if (item != null) {
                    addMessageComponent(item, messagePane)
                }
            }
            return
        } else {
            com = createTextComponent(message)
        }
        messagePane.add(com)
        messagePane.add(Box.createRigidArea(Dimension(0, borderWidth)))
    }

    private fun createTextComponent(message: Any): JComponent {
        var text = message.toString()
        if (!text.startsWith("<html>", true)) {
            val lines = text.lines()
            if (lines.size > 1) {
                text = lines.joinToString("<br/>", "<html>", "</html>")
            }
        }
        val label = mnemonicLabel(text)
        if (singleLabel) {
            label.alignmentX = JLabel.CENTER_ALIGNMENT
            label.horizontalAlignment = JLabel.CENTER
        } else {
            label.alignmentX = JLabel.LEFT_ALIGNMENT
        }
        return label
    }

    private fun createControlsPane(): JPanel? {
        if (options == null || options!!.isEmpty()) {
            return null
        }
        return createControlsPane(alignment, *prepareOptions())
    }

    private fun prepareOptions(): Array<Component> {
        val options = this.options ?: return emptyArray()
        val components = ArrayList<Component>(options.size)
        options.forEachIndexed { i, option ->
            val comp: Component = when (option) {
                is CharSequence -> JButton(OptionAction(option.toString(), i))
                is Item -> option.asAction(OptionListener((i))).asButton(option.style)
                is Component -> option
                is Action -> JButton(option)
                else -> throw IllegalArgumentException("selection require string or component: $option")
            }
            components.add(comp)
            if (i == this.selection) {
                if (comp !is JButton) {
                    throw IllegalArgumentException("default selection must be JButton: $comp")
                }
                defaultButton = comp
            }
        }
        return components.toTypedArray()
    }

    override fun cancelling() {
        super.cancelling()
        selection = -1
    }

    private inner class OptionListener(val index: Int) : CommandListener {
        override fun performed(command: String) {
            selection = index
            dispose()
        }
    }

    private inner class OptionAction(name: String, val index: Int) : AbstractAction(name) {
        override fun actionPerformed(e: ActionEvent) {
            selection = index
            dispose()
        }
    }
}
