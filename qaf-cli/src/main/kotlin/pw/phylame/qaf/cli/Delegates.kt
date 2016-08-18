package pw.phylame.qaf.cli

import org.apache.commons.cli.*
import pw.phylame.qaf.core.App
import pw.phylame.qaf.core.AppDelegate
import java.util.*

open class CLIDelegate(val defaultCommand: Command? = null,
                       val parser: CommandLineParser = DefaultParser()) : AppDelegate {
    val context = HashMap<String, Any>()

    lateinit var inputs: Array<String>
        private set

    protected open fun createOptions() {
    }

    fun addOption(option: Option, command: Command) {
        actions[option.opt] = command
        options.addOption(option)
    }

    fun addOption(option: Option, action: (CLIDelegate) -> Int) {
        addOption(option, object : Command {
            override fun execute(delegate: CLIDelegate): Int = action(delegate)
        })
    }

    fun addOption(name: String, option: Option, initializer: Initializer) {
        names[option.opt] = name
        actions[option.opt] = initializer
        options.addOption(option)
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
                } else if (action is Command) {
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
            commands.forEach {
                status = Math.min(status, it.execute(this))
            }
        } else if (defaultCommand != null) {
            status = defaultCommand.execute(this)
        }
        return status
    }

    override fun run() {
        parseOptions()
        if (!onOptionParsed()) {
            App.exit(-1)
        }
        App.exit(dispatchCommands())
    }

    private val actions = HashMap<String, Action>()

    internal val names = HashMap<String, String>()

    private val commands = LinkedList<Command>()

    private val options = Options()
}