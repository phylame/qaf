/*
 * Copyright 2015-2016 Peng Wan <phylame@163.com>
 *
 * This file is part of SCJ.
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

package qaf.cli

import org.apache.commons.cli.*
import qaf.core.App
import qaf.core.AppDelegate
import qaf.core.MapGetter
import java.util.*

open class CLIDelegate(val parser: CommandLineParser = DefaultParser()) : AppDelegate {
    val options = Options()

    var defaultCommand: Command? = null

    val context = HashMap<String, Any>()

    fun <T> managed(name: String = "", fallback: () -> T): MapGetter<T> = MapGetter(context, name, fallback)

    lateinit var inputs: Array<String>
        private set

    protected open fun createOptions() {}

    fun addOption(option: Option, action: Action) {
        actions[option.opt] = action
        options.addOption(option)
    }

    fun addOption(option: Option, action: (CLIDelegate) -> Int) {
        addOption(option, object : Command {
            override fun execute(delegate: CLIDelegate): Int = action(delegate)
        })
    }

    fun addOptionGroup(group: OptionGroup) {
        options.addOptionGroup(group)
    }

    protected open fun onOptionError(e: ParseException) {
        e.printStackTrace()
        App.exit(-1)
    }

    protected open fun onOptionParsed(): Boolean = true

    override fun onStart() {
        createOptions()
    }

    private fun parseOptions() {
        try {
            val cmd = parser.parse(options, App.arguments)
            for (option in cmd.options) {
                val action = actions[option.opt]
                if (action is Initializer) {
                    action.perform(this, cmd)
                }
                if (action is Command) {
                    commands.add(action)
                }
            }
            inputs = cmd.args
        } catch (e: ParseException) {
            onOptionError(e)
        }
    }

    private fun dispatchCommands(): Int {
        var status = 0
        if (commands.isNotEmpty()) {
            for (cmd in commands) {
                status = Math.min(status, cmd.execute(this))
            }
        } else if (defaultCommand != null) {
            status = defaultCommand!!.execute(this)
        }
        return status
    }

    override final fun run() {
        parseOptions()
        if (!onOptionParsed()) {
            App.exit(-1)
        }
        App.exit(dispatchCommands())
    }

    private val actions = HashMap<String, Action>()

    private val commands = LinkedHashSet<Command>()
}
