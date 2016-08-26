/*
 * Copyright 2016 Peng Wan <phylame@163.com>
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
import java.awt.Component
import javax.swing.*

enum class Type {
    PLAIN, RADIO, CHECK, TOGGLE
}

open class Item(val id: String, val enable: Boolean = true, val selected: Boolean = false, val type: Type = Type.PLAIN) {
    init {
        require(id.isNotEmpty()) { "id of action cannot be empty" }
    }
}

object Separator : Item("__SEPARATOR__")

open class Menu(id: String, val items: Array<Item>) : Item(id)

interface Designer {
    val menus: Array<Menu>?

    val toolbar: Array<Item>?
}

fun Item.asAction(listener: CommandListener, translator: Localizable = App, resource: Resource? = null): Action {
    require(id != "") { "empty id is only for separator" }
    require(this !is Menu) { "menu cannot be create as dispatcher action" }
    val action = DispatcherAction(id, listener, translator, resource)
    action.isEnabled = enable
    action.isSelected = selected
    return action
}

fun Menu.asMenu(translator: Localizable = App, resource: Resource? = null): JMenu {
    val menu = JMenu(IgnoredAction(id, translator, resource))
    menu.toolTipText = null
    return menu
}

fun Action.asMenuItem(type: Type, form: Form? = null): JMenuItem {
    val item = when (type) {
        Type.PLAIN -> JMenuItem(this)
        Type.CHECK -> JCheckBoxMenuItem(this)
        Type.RADIO -> JRadioButtonMenuItem(this)
        Type.TOGGLE -> throw IllegalArgumentException("type of toggle is not supported for menu item")
    }
    if (form != null) {
        item.performOn(form, this)
        item.toolTipText = null
    }
    return item
}

fun Action.asButton(type: Type, form: Form? = null): AbstractButton {
    val button: AbstractButton = when (type) {
        Type.PLAIN -> JButton(this)
        Type.CHECK -> JCheckBox(this)
        Type.RADIO -> JRadioButton(this)
        Type.TOGGLE -> {
            val result = JToggleButton(this)
            val icon: Icon? = this[IAction.SELECTED_ICON_KEY]
            if (icon != null) {
                result.selectedIcon = icon
            }
            result
        }
    }
    if (form != null) {
        button.performOn(form, this)
        button.toolTipText = null
    }
    return button
}

fun <T : JMenu> T.addItems(items: Array<Item>,
                           actions: MutableMap<String, Action>,
                           listener: CommandListener? = null,
                           translator: Localizable = App,
                           resource: Resource? = null,
                           form: Form? = null): T {
    popupMenu.addItems(items, actions, listener, translator, resource, form)
    return this
}

fun <T : JPopupMenu> T.addItems(items: Array<out Item>,
                                actions: MutableMap<String, Action>,
                                listener: CommandListener? = null,
                                translator: Localizable = App,
                                resource: Resource? = null,
                                form: Form? = null): T {
    var group: ButtonGroup? = null
    for (item in items) {
        val comp: JComponent = when (item) {
            Separator -> JPopupMenu.Separator()
            is Menu -> {
                val menu = item.asMenu(translator, resource)
                menu.addItems(item.items, actions, listener, translator, resource, form)
                menu
            }
            else -> {
                val result = actions.actionFor(item, listener, translator, resource).asMenuItem(item.type, form)
                if (item.type == Type.RADIO) {
                    if (group == null) {
                        group = ButtonGroup()
                    }
                    group.add(result)
                } else if (group != null) {
                    group = null
                }
                result
            }
        }
        add(comp)
    }
    return this
}

fun <T : JToolBar> T.addButton(button: AbstractButton): T {
    if (button.icon != null) {
        button.hideActionText = true
    }
    val action = button.action
    if (action != null) {
        var tip: String? = action[Action.SHORT_DESCRIPTION]
        if (tip != null && tip.isNotEmpty()) {
            val keyStroke: KeyStroke? = action[Action.ACCELERATOR_KEY]
            if (keyStroke != null) {
                tip += " (" + Ixin.formatKeyStroke(keyStroke) + ")"
            }
            button.toolTipText = tip
        }
    }
    button.isFocusable = false
    button.horizontalTextPosition = JButton.CENTER
    button.verticalTextPosition = JButton.BOTTOM
    add(button)
    return this
}

fun <T : JToolBar> T.addItems(items: Array<out Any>,
                              actions: MutableMap<String, Action>,
                              listener: CommandListener? = null,
                              translator: Localizable = App,
                              resource: Resource? = null,
                              form: Form? = null): T {
    var group: ButtonGroup? = null
    for (item in items) {
        when (item) {
            Separator -> addSeparator()
            is Item -> {
                val button = actions.actionFor(item.id, listener, translator, resource).asButton(item.type, form)
                addButton(button)
                if (item.type == Type.RADIO) {
                    if (item.type == Type.RADIO) {
                        if (group == null) {
                            group = ButtonGroup()
                        }
                        group.add(button)
                    } else if (group != null) {
                        group = null
                    }
                }
            }
            is Component -> add(item)
        }
    }
    return this
}
