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

package pw.phylame.qaf.ixin

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import pw.phylame.qaf.core.App
import pw.phylame.qaf.core.Localizable
import pw.phylame.ycl.log.Log
import java.awt.Component
import java.io.InputStream
import java.util.*
import javax.swing.*

enum class Style {
    PLAIN, RADIO, CHECK, TOGGLE;
}

open class Item(val id: String,
                val enable: Boolean = true,
                val selected: Boolean = false,
                val style: Style = Style.PLAIN) {
    init {
        require(id.isNotEmpty()) { "id of action cannot be empty" }
    }
}

open class Group(id: String, val items: Array<Item>) : Item(id)

object Separator : Item("__SEPARATOR__")

fun MutableMap<String, Action>.actionFor(item: Item,
                                         listener: CommandListener? = null,
                                         translator: Localizable = App,
                                         resource: Resource = Ixin.myDelegate.resource): Action = getOrPut(item.id) {
    item.asAction(listener ?: throw IllegalArgumentException("create action require listener"), translator, resource)
}

fun MutableMap<String, Action>.actionFor(id: String,
                                         listener: CommandListener? = null,
                                         translator: Localizable = App,
                                         resource: Resource = Ixin.myDelegate.resource): Action
        = actionFor(Item(id), listener, translator, resource)

interface Designer {
    val menus: Array<Group>?

    val toolbar: Array<Item>?
}


class BadDesignerException(message: String) : Exception(message)

open class JSONDesigner(input: InputStream) : Designer {
    constructor(path: String) : this(Ixin.myDelegate.resource.itemFor(path)?.openStream()
            ?: throw BadDesignerException("Not found designer in resource: $path"))

    companion object {
        private const val TAG = "JSONDesigner"
        const val MENUS_KEY = "menus"
        const val TOOLBAR_KEY = "toolbar"

        fun parseItems(array: JSONArray, items: MutableCollection<Item>) {
            for (item in array) {
                when (item) {
                    is String -> items.add(Item(item))
                    is JSONObject -> {
                        val id = item.getString("id")
                        val _array = item.optJSONArray("items")
                        if (_array != null) {
                            val _items = LinkedList<Item>()
                            parseItems(_array, _items)
                            items.add(Group(id, _items.toTypedArray()))
                        } else {
                            items.add(Item(id,
                                    item.optBoolean("enable", true),
                                    item.optBoolean("selected", false),
                                    Style.valueOf(item.optString("style", Style.PLAIN.name).toUpperCase())))
                        }
                    }
                    JSONObject.NULL -> items.add(Separator)
                    else -> throw BadDesignerException("Unexpected style of item: ${item.javaClass}")
                }
            }
        }
    }

    private val _menus = LinkedList<Group>()

    private val _toolbar = LinkedList<Item>()

    init {
        val json = JSONObject(JSONTokener(input))
        try {
            for (item in json.getJSONArray(MENUS_KEY)) {
                if (item !is JSONObject) {
                    throw BadDesignerException("Require 'object' in $MENUS_KEY")
                }
                val id = item.getString("id")
                val items = LinkedList<Item>()
                val array = item.optJSONArray("items")
                if (array != null) {
                    parseItems(array, items)
                }
                _menus.add(Group(id, items.toTypedArray()))
            }
        } catch (e: JSONException) {
            Log.d(TAG, e)
        }
        try {
            parseItems(json.getJSONArray(TOOLBAR_KEY), _toolbar)
        } catch (e: JSONException) {
            Log.d(TAG, e)
        }
    }

    override val menus: Array<Group> = _menus.toTypedArray()

    override val toolbar: Array<Item> = _toolbar.toTypedArray()
}

fun Item.asAction(listener: CommandListener,
                  translator: Localizable = App,
                  resource: Resource = Ixin.myDelegate.resource): Action {
    require(id != "") { "empty id is only for separator" }
    require(this !is Group) { "group cannot be create as dispatcher action" }
    val action = DispatcherAction(id, listener, translator, resource)
    action.isEnabled = enable
    action.isSelected = selected
    return action
}

fun Group.asMenu(translator: Localizable = App, resource: Resource = Ixin.myDelegate.resource): JMenu {
    val menu = JMenu(IgnoredAction(id, translator, resource))
    menu.toolTipText = null
    return menu
}

fun Action.asMenuItem(style: Style, form: Form? = null): JMenuItem {
    val item = when (style) {
        Style.PLAIN -> JMenuItem(this)
        Style.CHECK -> JCheckBoxMenuItem(this)
        Style.RADIO -> JRadioButtonMenuItem(this)
        Style.TOGGLE -> throw IllegalArgumentException("style of toggle is not supported for menu item")
    }
    if (form != null) {
        item.performOn(form, this).toolTipText = null
    }
    return item
}

fun Action.asButton(style: Style, form: Form? = null): AbstractButton {
    val button: AbstractButton = when (style) {
        Style.PLAIN -> JButton(this)
        Style.CHECK -> JCheckBox(this)
        Style.RADIO -> JRadioButton(this)
        Style.TOGGLE -> {
            val result = JToggleButton(this)
            val icon: Icon? = this[IAction.SELECTED_ICON_KEY]
            if (icon != null) {
                result.selectedIcon = icon
            }
            result
        }
    }
    if (form != null) {
        button.performOn(form, this).toolTipText = null
    }
    return button
}

fun <T : JMenu> T.addItems(items: Array<Item>,
                           actions: MutableMap<String, Action>,
                           listener: CommandListener? = null,
                           translator: Localizable = App,
                           resource: Resource = Ixin.myDelegate.resource,
                           form: Form? = null): T {
    popupMenu.addItems(items, actions, listener, translator, resource, form)
    return this
}

fun <T : JPopupMenu> T.addItems(items: Array<out Item>,
                                actions: MutableMap<String, Action>,
                                listener: CommandListener? = null,
                                translator: Localizable = App,
                                resource: Resource = Ixin.myDelegate.resource,
                                form: Form? = null): T {
    var group: ButtonGroup? = null
    for (item in items) {
        val comp: JComponent = when (item) {
            Separator -> JPopupMenu.Separator()
            is Group -> item.asMenu(translator, resource).addItems(item.items, actions, listener, translator, resource, form)
            else -> {
                val result = actions.actionFor(item, listener, translator, resource).asMenuItem(item.style, form)
                if (item.style == Style.RADIO) {
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
                              resource: Resource = Ixin.myDelegate.resource,
                              form: Form? = null): T {
    var group: ButtonGroup? = null
    for (item in items) {
        when (item) {
            Separator -> addSeparator()
            is Item -> {
                val button = actions.actionFor(item.id, listener, translator, resource).asButton(item.style, form)
                addButton(button)
                if (item.style == Style.RADIO) {
                    if (item.style == Style.RADIO) {
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
