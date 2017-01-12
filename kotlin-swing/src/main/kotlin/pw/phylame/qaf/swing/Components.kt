package pw.phylame.qaf.swing

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.CompoundBorder

fun <T : Container> T.panel(adding: Boolean = true, init: JPanel.() -> Unit): JPanel {
    val panel = JPanel()
    if (adding) {
        add(panel)
    }
    panel.init()
    return panel
}

fun <T : Container> T.scrollPane(adding: Boolean = true, init: JScrollPane.() -> Unit): JScrollPane {
    val scrollPane = JScrollPane()
    if (adding) {
        add(scrollPane)
    }
    scrollPane.init()
    return scrollPane
}

fun <T : Container> T.tabbedPane(adding: Boolean = true, init: JTabbedPane.() -> Unit): JTabbedPane {
    val tabbedPane = JTabbedPane()
    if (adding) {
        add(tabbedPane)
    }
    tabbedPane.init()
    return tabbedPane
}

fun <T : Container> T.splitPane(adding: Boolean = true, init: JSplitPane.() -> Unit): JSplitPane {
    val splitPane = JSplitPane()
    if (adding) {
        add(splitPane)
    }
    splitPane.init()
    return splitPane
}

fun <T : Container> T.toolBar(adding: Boolean = true, init: JToolBar.() -> Unit): JToolBar {
    val toolBar = JToolBar()
    if (adding) {
        add(toolBar)
    }
    toolBar.init()
    return toolBar
}

fun <T : Container> T.label(adding: Boolean = true, init: JLabel.() -> Unit): JLabel {
    val comp = JLabel()
    if (adding) {
        add(comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.button(adding: Boolean = true, init: JButton.() -> Unit): JButton {
    val comp = JButton()
    if (adding) {
        add(comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.checkBox(adding: Boolean = true, init: JCheckBox.() -> Unit): JCheckBox {
    val comp = JCheckBox()
    if (adding) {
        add(comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.radioButton(adding: Boolean = true, init: JRadioButton.() -> Unit): JRadioButton {
    val comp = JRadioButton()
    if (adding) {
        add(comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.buttonGroup(init: T.(ButtonGroup) -> Unit): ButtonGroup {
    val group = ButtonGroup()
    init(group)
    return group
}

fun <T : Container, V> T.comboBox(adding: Boolean = true, init: JComboBox<V>.() -> Unit): JComboBox<V> {
    val comp = JComboBox<V>()
    if (adding) {
        add(comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.textField(adding: Boolean = true, init: JTextField.() -> Unit): JTextField {
    val comp = JTextField()
    if (adding) {
        add(comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.textArea(adding: Boolean = true, init: JTextArea.() -> Unit): JTextArea {
    val comp = JTextArea()
    if (adding) {
        add(comp)
    }
    comp.init()
    return comp
}

var Container.north: Component
    get() = throw UnsupportedOperationException()
    set(value) {
        check(layout is BorderLayout) { "Require for BorderLayout" }
        add(value, BorderLayout.NORTH)
    }

var Container.east: Component
    get() = throw UnsupportedOperationException()
    set(value) {
        check(layout is BorderLayout) { "Require for BorderLayout" }
        add(value, BorderLayout.EAST)
    }

var Container.south: Component
    get() = throw UnsupportedOperationException()
    set(value) {
        check(layout is BorderLayout) { "Require for BorderLayout" }
        add(value, BorderLayout.SOUTH)
    }

var Container.west: Component
    get() = throw UnsupportedOperationException()
    set(value) {
        check(layout is BorderLayout) { "Require for BorderLayout" }
        add(value, BorderLayout.WEST)
    }

var Container.center: Component
    get() = throw UnsupportedOperationException()
    set(value) {
        check(layout is BorderLayout) { "Require for BorderLayout" }
        add(value, BorderLayout.CENTER)
    }

var <T : JSplitPane> T.aboveComponent: Component
    get() {
        return leftComponent
    }
    set(value) {
        leftComponent = value
    }

var <T : JSplitPane> T.belowComponent: Component
    get() {
        return rightComponent
    }
    set(value) {
        rightComponent = value
    }

fun <T : AbstractButton> T.actionListener(block: (ActionEvent) -> Unit) {
    addActionListener(BlockActionListener(block))
}

operator fun Border.plus(border: Border): Border = CompoundBorder(border, this)
