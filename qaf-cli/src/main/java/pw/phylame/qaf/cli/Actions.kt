package pw.phylame.qaf.cli

import org.apache.commons.cli.CommandLine
import pw.phylame.qaf.core.App
import pw.phylame.qaf.core.Converters

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

interface ValueFetcher<out T : Any> : Initializer {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        val value = parse(cmd.getOptionValue(option))
        if (value == null) {
            App.exit(-1)
        } else {
            delegate.context[delegate.names[option]!!] = value
        }
    }

    val option: String

    fun parse(value: String): T? = null
}

class TypedFetcher<T : Any>(override val option: String, val clazz: Class<T>) : ValueFetcher<T> {
    override fun parse(value: String): T? {
        return Converters.parse(value, clazz)
    }
}

inline fun <reified T : Any> fetcherOf(option: String): TypedFetcher<T> = TypedFetcher(option, T::class.java)

class ListFetcher(val option: String) : Initializer {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[delegate.names[option]!!] = cmd.getOptionValues(option)
    }
}

class PropertiesFetcher(val option: String) : Initializer {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[delegate.names[option]!!] = cmd.getOptionProperties(option)
    }
}

class Switcher(val option: String) : Initializer {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[delegate.names[option]!!] = true
    }
}
