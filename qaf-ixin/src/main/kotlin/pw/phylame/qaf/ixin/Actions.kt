package pw.phylame.qaf.ixin

import javax.swing.Action
import kotlin.reflect.KProperty

const val NORMAL_ICON_SUFFIX = "icon"
const val SELECTED_ICON_SUFFIX = "icon.selected"
const val SHOWY_ICON_SUFFIX = "icon.showy"
const val NAME_SUFFIX = "name"
const val SCOPE_SUFFIX = "scope"
const val SHORTCUT_SUFFIX = "shortcut"
const val MNEMONIC_SUFFIX = "mnemonic"
const val DETAILS_SUFFIX = "details"
const val TIP_SUFFIX = "tip"

@Suppress("unchecked_cast")
operator fun <T> Action.getValue(ref: Any?, property: KProperty<*>): T? = getValue(property.name) as? T

operator fun <T> Action.setValue(ref: Any?, property: KProperty<*>, value: T) {
    putValue(property.name, value)
}
