package pw.phylame.qaf.swing

import javax.swing.*

fun <T : JMenuBar> T.menu(init: JMenu.() -> Unit): JMenu {
    val menu = JMenu()
    add(menu)
    menu.init()
    return menu
}

fun <T : JMenu> T.separator() {
    add(JPopupMenu.Separator())
}

fun <T : JMenu> T.item(init: JMenuItem.() -> Unit): JMenuItem {
    val item = JMenuItem()
    add(item)
    item.init()
    return item
}

fun <T : JMenu> T.check(init: JCheckBoxMenuItem.() -> Unit): JCheckBoxMenuItem {
    val item = JCheckBoxMenuItem()
    add(item)
    item.init()
    return item
}

fun <T : JMenu> T.radio(init: JRadioButtonMenuItem.() -> Unit): JRadioButtonMenuItem {
    val item = JRadioButtonMenuItem()
    add(item)
    item.init()
    return item
}
