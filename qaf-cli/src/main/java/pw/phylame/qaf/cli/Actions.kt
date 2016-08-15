package pw.phylame.qaf.cli

import org.apache.commons.cli.CommandLine
import pw.phylame.qaf.core.App

interface Action

interface Command : Action {
    fun execute(delegate: CLIDelegate): Int
}

interface Initializer<T : Any> : Action {
    fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        val value = parse(cmd.getOptionValue(option))
        if (value == null) {
            App.exit(-1)
        } else {
            delegate.context[option] = value
        }
    }

    val option: String

    fun parse(value: String): T? = null
}

abstract class AbstractInitializer<T : Any>(override val option: String) : Initializer<T>

class BooleanFetcher(option: String) : AbstractInitializer<Boolean>(option) {
    override fun parse(value: String): Boolean? = value.toBoolean()
}

class IntegerFetcher(option: String) : AbstractInitializer<Int>(option) {
    override fun parse(value: String): Int? = value.toInt()
}

class DoubleFetcher(option: String) : AbstractInitializer<Double>(option) {
    override fun parse(value: String): Double? = value.toDouble()
}

class ListFetcher(override val option: String) : Initializer<Array<String>> {
    override fun perform(delegate: CLIDelegate, cmd: CommandLine) {
        delegate.context[option] = cmd.getOptionValues(option)
    }
}