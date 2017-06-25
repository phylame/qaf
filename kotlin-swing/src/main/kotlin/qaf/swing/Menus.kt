package qaf.swing

import javax.swing.*

fun <T : JMenuBar> T.menu(adding: Boolean = true, init: JMenu.() -> Unit): JMenu {
    val menu = JMenu()
    if (adding) {
        add(menu)
    }
    menu.init()
    return menu
}

fun <T : JMenu> T.menu(adding: Boolean = true, init: JMenu.() -> Unit): JMenu {
    val menu = JMenu()
    if (adding) {
        add(menu)
    }
    menu.init()
    return menu
}

fun <T : JMenu> T.separator() {
    add(JPopupMenu.Separator())
}

fun <T : JMenu> T.item(adding: Boolean = true, init: JMenuItem.() -> Unit): JMenuItem {
    val item = JMenuItem()
    if (adding) {
        add(item)
    }
    item.init()
    return item
}

fun <T : JMenu> T.check(adding: Boolean = true, init: JCheckBoxMenuItem.() -> Unit): JCheckBoxMenuItem {
    val item = JCheckBoxMenuItem()
    if (adding) {
        add(item)
    }
    item.init()
    return item
}

fun <T : JMenu> T.radio(adding: Boolean = true, init: JRadioButtonMenuItem.() -> Unit): JRadioButtonMenuItem {
    val item = JRadioButtonMenuItem()
    if (adding) {
        add(item)
    }
    item.init()
    return item
}
