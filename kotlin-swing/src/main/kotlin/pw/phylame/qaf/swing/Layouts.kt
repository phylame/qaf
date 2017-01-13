package pw.phylame.qaf.swing

import java.awt.*
import javax.swing.BoxLayout
import javax.swing.GroupLayout
import javax.swing.SpringLayout

fun <T : Container> T.flowLayout(align: Int = FlowLayout.CENTER, hgap: Int = 5, vgap: Int = 5, init: T.(FlowLayout) -> Unit): FlowLayout {
    val layout = FlowLayout(align, hgap, vgap)
    this.layout = layout
    init(layout)
    return layout
}

fun <T : Container> T.borderLayout(hgap: Int = 0, vgap: Int = 0, init: T.(BorderLayout) -> Unit): BorderLayout {
    val layout = BorderLayout(hgap, vgap)
    this.layout = layout
    init(layout)
    return layout
}

fun <T : Container> T.gridLayout(rows: Int = 1, cols: Int = 0, hgap: Int = 0, vgap: Int = 0, init: T.(GridLayout) -> Unit): GridLayout {
    val layout = GridLayout(rows, cols, hgap, vgap)
    this.layout = layout
    init(layout)
    return layout
}

fun <T : Container> T.cardLayout(hgap: Int = 0, vgap: Int = 0, init: T.(CardLayout) -> Unit): CardLayout {
    val layout = CardLayout(hgap, vgap)
    this.layout = layout
    init(layout)
    return layout
}

fun <T : Container> T.gridBagLayout(init: T.(GridBagLayout) -> Unit): GridBagLayout {
    val layout = GridBagLayout()
    this.layout = layout
    init(layout)
    return layout
}

fun <T : Container> T.boxLayout(axis: Int = BoxLayout.LINE_AXIS, init: T.(BoxLayout) -> Unit): BoxLayout {
    val layout = BoxLayout(this, axis)
    this.layout = layout
    init(layout)
    return layout
}

fun <T : Container> T.groupLayout(init: T.(GroupLayout) -> Unit): GroupLayout {
    val layout = GroupLayout(this)
    this.layout = layout
    init(layout)
    return layout
}

fun <T : Container> T.springLayout(init: T.(SpringLayout) -> Unit): SpringLayout {
    val layout = SpringLayout()
    this.layout = layout
    init(layout)
    return layout
}


