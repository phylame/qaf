import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import java.awt.event.ActionEvent
import javax.swing.*

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
    val label = JLabel()
    if (adding) {
        add(label)
    }
    label.init()
    return label
}

fun <T : Container> T.button(adding: Boolean = true, init: JButton.() -> Unit): JButton {
    val button = JButton()
    if (adding) {
        add(button)
    }
    button.init()
    return button
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
