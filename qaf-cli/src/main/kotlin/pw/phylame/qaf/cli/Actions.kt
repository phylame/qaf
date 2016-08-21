package pw.phylame.qaf.cli

import org.apache.commons.cli.CommandLine
import pw.phylame.qaf.core.App
import pw.phylame.ycl.format.Converters

interface Action

interface Command : Action {
    fun execute(delegate: CLIDelegate): Int
}

abstract class SubCommand(val error: String = "no input") : Command {
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
                delegate.context[delegate.names[option]!!] = value
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
    override fun parse(value: String): T? {
        return Converters.parse(value, clazz)
    }
}

inline fun <reified T : Any> fetcherOf(option: String): TypedFetcher<T> = TypedFetcher(option, T::class.java)

fun <T : Any> fetcherOf(option: String, clazz: Class<T>, validator: ((T) -> Boolean)? = null): TypedFetcher<T> =
        TypedFetcher(option, clazz, validator)

open class ListFetcher(val option: String) : Initializer {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[delegate.names[option]!!] = cmd.getOptionValues(option)
    }
}

open class PropertiesFetcher(val option: String) : Initializer {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[delegate.names[option]!!] = cmd.getOptionProperties(option)
    }
}

open class Switcher(val option: String) : Initializer {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[delegate.names[option]!!] = true
    }
}
