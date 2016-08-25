package pw.phylame.qaf.ixin

import pw.phylame.qaf.core.App
import pw.phylame.qaf.core.Localizable
import pw.phylame.ycl.log.Log
import java.awt.event.ActionEvent
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.KeyStroke

private const val TAG = "ACTION"

operator fun <T : Any> Action.set(name: String, value: T?) {
    putValue(name, value)
}

@Suppress("unchecked_cast")
operator fun <T : Any> Action.get(name: String): T? = getValue(name) as? T

var Action.isSelected: Boolean get() = getValue(Action.SELECTED_KEY) as? Boolean ?: false
    set(value) {
        putValue(Action.SELECTED_KEY, value)
    }

abstract class IAction(id: String, val translator: Localizable = App, var resource: Resource? = null) : AbstractAction() {
    companion object {
        const val SELECTED_ICON_KEY = "IxinSelectedIcon"

        const val SCOPE_KEY = "IxinScopeKey"

        var scopeSuffix = ".scope"
        var normalIconSuffix = ".icon"
        var selectedIconSuffix = "-selected"
        var showyIconSuffix = "-showy"
        var shortcutKeySuffix = ".shortcut"
        var tipTextSuffix = ".tip"
        var detailsTextSuffix = ".details"

        var iconPrefix = "actions/"
        var iconSuffix = ".png"
    }

    init {
        putValue(Action.ACTION_COMMAND_KEY, id)

        if (resource == null) {
            if (App.delegate !is IxinDelegate<*>) {
                Log.w(TAG, "action should be used in Ixin app")
            } else {
                resource = (App.delegate as IxinDelegate<*>).resource
            }
        }

        // name and mnemonic
        var text = textOf(id)
        if (text != null) {
            val result = Ixin.splitMnemonic(text)
            putValue(Action.NAME, result.name)
            if (Ixin.mnemonicEnable && result.mnemonic != 0) {
                putValue(Action.MNEMONIC_KEY, result.mnemonic)
                putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, result.index)
            }
        }

        // scope
        text = textOf(id + scopeSuffix)
        if (text != null) {
            putValue(SCOPE_KEY, text)
        }

        // icons
        val path = textOf(id + normalIconSuffix) ?: if (resource != null) iconPrefix + id + iconSuffix else null
        if (path != null) {
            putValue(Action.SMALL_ICON, resource?.getIcon(path))
            putValue(Action.LARGE_ICON_KEY, resource?.getIcon(path, showyIconSuffix))
            putValue(SELECTED_ICON_KEY, resource?.getIcon(path, selectedIconSuffix))
        }

        // menu accelerator
        text = textOf(id + shortcutKeySuffix)
        if (text != null) {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(text))
        }

        // tip
        text = textOf(id + tipTextSuffix)
        if (text != null) {
            putValue(Action.SHORT_DESCRIPTION, text)
        }

        // details
        text = textOf(id + detailsTextSuffix)
        if (text != null) {
            putValue(Action.LONG_DESCRIPTION, text)
        }
    }

    private fun textOf(key: String): String? = try {
        translator.get(key).let { if (it.isNotEmpty()) it else null }
    } catch (e: MissingResourceException) {
        null
    }
}

annotation class Actioned(val name: String = "")

interface CommandListener {
    fun commandPerformed(command: String)
}

class DispatcherAction(val listener: CommandListener,
                       id: String,
                       translator: Localizable = App,
                       resource: Resource? = null) : IAction(id, translator, resource) {
    override fun actionPerformed(e: ActionEvent) {
        listener.commandPerformed(e.actionCommand)
    }
}

/**
 * Dispatches command to the proxy object.
 */
open class CommandDispatcher(val proxy: Any) : CommandListener {
    private val methods = HashMap<String, Method>()

    init {
        proxy.javaClass.methods.filter {
            Modifier.isPublic(it.modifiers) && !Modifier.isStatic(it.modifiers) && !Modifier.isAbstract(it.modifiers)
        }.forEach {
            val actioned = it.getAnnotation(Actioned::class.java)
            if (actioned != null) {
                methods.put(if (actioned.name.isNotEmpty()) actioned.name else it.name, it)
            }
        }
    }

    override fun commandPerformed(command: String) {
        try {
            methods[command]?.invoke(proxy) ?: throw RuntimeException("No such method of proxy for command: $command")
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot execute command: $command", e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Cannot execute command: $command", e)
        }
    }
}
