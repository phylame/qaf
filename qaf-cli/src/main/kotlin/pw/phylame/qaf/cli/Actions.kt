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

package pw.phylame.qaf.cli

import org.apache.commons.cli.CommandLine
import pw.phylame.qaf.core.App
import pw.phylame.ycl.format.Converters

interface Action

interface Command : Action {
    fun execute(delegate: CLIDelegate): Int
}

abstract class SubCommand(val error: String = "no command found") : Command {
    override fun execute(delegate: CLIDelegate): Int {
        if (delegate.inputs.isEmpty()) {
            App.error(error)
            return -1
        }
        return onCommand(delegate.inputs.first(), delegate.inputs.slice(1..delegate.inputs.size - 1))
    }

    protected abstract fun onCommand(name: String, args: List<String>): Int
}

interface Initializer : Action {
    fun perform(delegate: CLIDelegate, cmd: CommandLine)
}

interface ValueFetcher<T : Any> : Initializer {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        val value = parse(cmd.getOptionValue(option))
        if (value == null) {
            App.exit(-1)
        } else {
            if (validator?.invoke(value) ?: true) {
                delegate.context[option] = value
            } else {
                App.exit(-1)
            }
        }
    }

    val option: String

    val validator: ((T) -> Boolean)?

    fun parse(value: String): T? = null
}

open class TypedFetcher<T : Any>(override val option: String,
                                 val clazz: Class<T>,
                                 override val validator: ((T) -> Boolean)? = null) : ValueFetcher<T> {
    override fun parse(value: String): T? = Converters.parse(value, clazz)
}

inline fun <reified T : Any> fetcherOf(option: String): TypedFetcher<T> = TypedFetcher(option, T::class.java)

fun <T : Any> fetcherOf(option: String, clazz: Class<T>, validator: ((T) -> Boolean)? = null): TypedFetcher<T> =
        TypedFetcher(option, clazz, validator)

abstract class SingleInitializer(val option: String) : Initializer {
    private var performed = false

    override final fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        // only perform once
        if (!performed) {
            init(delegate, cmd)
            performed = true
        }
    }

    protected abstract fun init(delegate: CLIDelegate, cmd: CommandLine)
}

open class ListFetcher(option: String) : SingleInitializer(option) {
    override fun init(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[option] = cmd.getOptionValues(option)
    }
}

open class PropertiesFetcher(option: String) : SingleInitializer(option) {
    override fun init(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[option] = cmd.getOptionProperties(option)
    }
}

open class Switcher(val option: String) : Initializer {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[option] = true
    }
}
