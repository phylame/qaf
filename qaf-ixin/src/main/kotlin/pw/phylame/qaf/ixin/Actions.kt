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

import pw.phylame.qaf.core.App
import pw.phylame.qaf.core.Localizable
import java.awt.event.ActionEvent
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.KeyStroke

@Suppress("unchecked_cast")
operator fun <T : Any> Action.get(name: String): T? = getValue(name) as? T

operator fun <T : Any> Action.set(name: String, value: T?) {
    putValue(name, value)
}

var Action.isSelected: Boolean get() = getValue(Action.SELECTED_KEY) as? Boolean ?: false
    set(value) {
        putValue(Action.SELECTED_KEY, value)
    }

abstract class IAction(id: String,
                       translator: Localizable = App,
                       resource: Resource = Ixin.delegate.resource) : AbstractAction() {
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
        val path = translator.getOr(id + normalIconSuffix) ?: iconPrefix + id + iconSuffix
        putValue(Action.SMALL_ICON, resource.iconFor(path))
        putValue(Action.LARGE_ICON_KEY, resource.iconFor(path, showyIconSuffix))
        putValue(SELECTED_ICON_KEY, resource.iconFor(path, selectedIconSuffix))

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
                    resource: Resource = Ixin.delegate.resource) : IAction(id, translator, resource) {
    override fun actionPerformed(e: ActionEvent) {
        // do nothing
    }
}

class DispatcherAction(id: String,
                       val listener: CommandListener,
                       translator: Localizable = App,
                       resource: Resource = Ixin.delegate.resource) : IAction(id, translator, resource) {
    override fun actionPerformed(e: ActionEvent) {
        listener.performed(e.actionCommand)
    }
}

annotation class Command(val value: String = "")

interface CommandListener {
    fun performed(command: String)
}

/**
 * Dispatches command to the proxy object.
 */
open class CommandDispatcher(proxies: Array<Any>) : CommandListener {
    private val invocations = HashMap<String, Invocation>()

    init {
        for (proxy in proxies) {
            addProxy(proxy)
        }
    }

    fun addProxy(proxy: Any) {
        proxy.javaClass.methods.filter {
            Modifier.isPublic(it.modifiers)
                    && !Modifier.isStatic(it.modifiers)
                    && !Modifier.isAbstract(it.modifiers)
                    && it.parameterTypes.isEmpty()
        }.forEach {
            val command = it.getAnnotation(Command::class.java)
            if (command != null) {
                invocations.put(if (command.value.isNotEmpty()) command.value else it.name, Invocation(proxy, it))
            }
        }
    }

    override final fun performed(command: String) {
        invocations[command]?.invoke() ?: throw RuntimeException("No such method of proxy for command: $command")
    }

    data class Invocation(val proxy: Any, val method: Method) {
        fun invoke() {
            method.invoke(proxy)
        }
    }
}
