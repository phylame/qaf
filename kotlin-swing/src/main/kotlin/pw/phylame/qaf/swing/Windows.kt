package pw.phylame.qaf.swing

import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JMenuBar

fun frame(init: JFrame.() -> Unit): JFrame {
    val frame = JFrame()
    frame.init()
    return frame
}

fun dialog(init: JDialog.() -> Unit): JDialog {
    val dialog = JDialog()
    dialog.init()
    return dialog
}

fun <T : JFrame> T.menuBar(init: JMenuBar.() -> Unit): JMenuBar {
    val menuBar = JMenuBar()
    jMenuBar = menuBar
    menuBar.init()
    return menuBar
}

fun <T : JDialog> T.menuBar(init: JMenuBar.() -> Unit): JMenuBar {
    val menuBar = JMenuBar()
    jMenuBar = menuBar
    menuBar.init()
    return menuBar
}
