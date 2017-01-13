package pw.phylame.qaf.swing

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.Border
import javax.swing.border.CompoundBorder

fun <T : Container> T.pane(adding: Boolean = true, init: JPanel.() -> Unit): JPanel {
    val pane = JPanel()
    if (adding) {
        addComponent(this, pane)
    }
    pane.init()
    return pane
}

fun <T : Container> T.scrollPane(adding: Boolean = true, init: JScrollPane.() -> Unit): JScrollPane {
    val pane = JScrollPane()
    if (adding) {
        addComponent(this, pane)
    }
    pane.init()
    return pane
}

fun <T : Container> T.tabbedPane(adding: Boolean = true, init: JTabbedPane.() -> Unit): JTabbedPane {
    val pane = JTabbedPane()
    if (adding) {
        addComponent(this, pane)
    }
    pane.init()
    return pane
}

fun <T : Container> T.splitPane(adding: Boolean = true, init: JSplitPane.() -> Unit): JSplitPane {
    val pane = JSplitPane()
    if (adding) {
        addComponent(this, pane)
    }
    pane.init()
    return pane
}

fun <T : Container> T.toolBar(adding: Boolean = true, init: JToolBar.() -> Unit): JToolBar {
    val comp = JToolBar()
    if (adding) {
        addComponent(this, comp)
    }
    comp.init()
    return comp
}

fun <T : JToolBar> T.separator(size: Dimension? = null) {
    addSeparator(size)
}

fun <T : Container> T.label(adding: Boolean = true, init: JLabel.() -> Unit): JLabel {
    val comp = JLabel()
    if (adding) {
        addComponent(this, comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.button(adding: Boolean = true, init: JButton.() -> Unit): JButton {
    val comp = JButton()
    if (adding) {
        addComponent(this, comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.checkBox(adding: Boolean = true, init: JCheckBox.() -> Unit): JCheckBox {
    val comp = JCheckBox()
    if (adding) {
        addComponent(this, comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.radioButton(adding: Boolean = true, init: JRadioButton.() -> Unit): JRadioButton {
    val comp = JRadioButton()
    if (adding) {
        addComponent(this, comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.buttonGroup(init: T.(ButtonGroup) -> Unit): ButtonGroup {
    val group = ButtonGroup()
    init(group)
    return group
}

fun <T : Container> T.textField(adding: Boolean = true, init: JTextField.() -> Unit): JTextField {
    val comp = JTextField()
    if (adding) {
        addComponent(this, comp)
    }
    comp.init()
    return comp
}

fun <T : Container> T.textArea(adding: Boolean = true, init: JTextArea.() -> Unit): JTextArea {
    val comp = JTextArea()
    if (adding) {
        addComponent(this, comp)
    }
    comp.init()
    return comp
}

fun <T : Container, V> T.comboBox(adding: Boolean = true, init: JComboBox<V>.() -> Unit): JComboBox<V> {
    val comp = JComboBox<V>()
    if (adding) {
        addComponent(this, comp)
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

fun <T : JTabbedPane> T.tab(title: String, builder: T.() -> Component): Unit {
    addTab(title, builder())
}

var JScrollPane.content: Component?
    get() = viewport?.view
    set(value) {
        setViewportView(value)
    }

var JSplitPane.aboveComponent: Component
    get() = leftComponent
    set(value) {
        leftComponent = value
    }

var JSplitPane.belowComponent: Component
    get() = rightComponent
    set(value) {
        rightComponent = value
    }

operator fun Border.plus(outside: Border): Border = CompoundBorder(outside, this)

private fun addComponent(container: Container, component: Component) {
    when (container) {
        is JScrollPane -> container.setViewportView(component)
        else -> container.add(component)
    }
}
