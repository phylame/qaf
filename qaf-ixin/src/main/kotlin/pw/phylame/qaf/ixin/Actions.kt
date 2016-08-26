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

fun MutableMap<String, Action>.actionFor(item: Item,
                                         listener: CommandListener? = null,
                                         translator: Localizable = App,
                                         resource: Resource? = null): Action = getOrPut(item.id) {
    item.asAction(listener ?: throw IllegalArgumentException("create action require commandListener"), translator, resource)
}

fun MutableMap<String, Action>.actionFor(id: String,
                                         listener: CommandListener? = null,
                                         translator: Localizable = App,
                                         resource: Resource? = null): Action = actionFor(Item(id), listener, translator, resource)

var Action.isSelected: Boolean get() = getValue(Action.SELECTED_KEY) as? Boolean ?: false
    set(value) {
        putValue(Action.SELECTED_KEY, value)
    }

abstract class IAction(id: String, translator: Localizable = App, var resource: Resource? = null) : AbstractAction() {
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
        var text = translator.getOr(id)
        if (text != null) {
            val result = Ixin.mnemonicOf(text)
            putValue(Action.NAME, result.name)
            if (result.isEnable) {
                putValue(Action.MNEMONIC_KEY, result.mnemonic)
                putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, result.index)
            }
        }

        // scope
        text = translator.getOr(id + scopeSuffix)
        if (text != null) {
            putValue(SCOPE_KEY, text)
        }

        // icons
        val path = translator.getOr(id + normalIconSuffix) ?: if (resource != null) iconPrefix + id + iconSuffix else null
        if (path != null && resource != null) {
            putValue(Action.SMALL_ICON, resource!!.iconFor(path))
            putValue(Action.LARGE_ICON_KEY, resource!!.iconFor(path, showyIconSuffix))
            putValue(SELECTED_ICON_KEY, resource!!.iconFor(path, selectedIconSuffix))
        }

        // menu accelerator
        text = translator.getOr(id + shortcutKeySuffix)
        if (text != null) {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(text))
        }

        // tip
        text = translator.getOr(id + tipTextSuffix)
        if (text != null) {
            putValue(Action.SHORT_DESCRIPTION, text)
        }

        // details
        text = translator.getOr(id + detailsTextSuffix)
        if (text != null) {
            putValue(Action.LONG_DESCRIPTION, text)
        }
    }
}

class IgnoredAction(id: String,
                    translator: Localizable = App,
                    resource: Resource? = null) : IAction(id, translator, resource) {
    override fun actionPerformed(e: ActionEvent?) {
        // do nothing
    }
}

annotation class Actioned(val value: String = "")

interface CommandListener {
    fun commandPerformed(command: String)
}

class DispatcherAction(id: String,
                       val listener: CommandListener,
                       translator: Localizable = App,
                       resource: Resource? = null) : IAction(id, translator, resource) {
    override fun actionPerformed(e: ActionEvent) {
        listener.commandPerformed(e.actionCommand)
    }
}

/**
 * Dispatches command to the proxy object.
 */
open class CommandDispatcher(val proxies: Array<Any>) : CommandListener {
    private val invocations = HashMap<String, Invocation>()

    init {
        for (proxy in proxies) {
            prepareProxy(proxy)
        }
    }

    private fun prepareProxy(proxy: Any) {
        proxy.javaClass.methods.filter {
            Modifier.isPublic(it.modifiers) && !Modifier.isStatic(it.modifiers) && !Modifier.isAbstract(it.modifiers) && it.parameterTypes.isEmpty()
        }.forEach {
            val actioned = it.getAnnotation(Actioned::class.java)
            if (actioned != null) {
                invocations.put(if (actioned.value.isNotEmpty()) actioned.value else it.name, Invocation(proxy, it))
            }
        }
    }

    override fun commandPerformed(command: String) {
        try {
            val invocation = invocations[command]
            if (invocation != null) {
                invocation.method.invoke(invocation.proxy)
            } else {
                throw RuntimeException("No such method of proxy for command: $command")
            }
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot execute command: $command", e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Cannot execute command: $command", e)
        }
    }

    data class Invocation(val proxy: Any, val method: Method)
}
