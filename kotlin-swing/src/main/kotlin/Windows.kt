import javax.swing.JFrame
import javax.swing.JMenuBar

fun <T : JFrame> T.menuBar(init: JMenuBar.() -> Unit): JMenuBar {
    val menuBar = JMenuBar()
    jMenuBar = menuBar
    menuBar.init()
    return menuBar
}
