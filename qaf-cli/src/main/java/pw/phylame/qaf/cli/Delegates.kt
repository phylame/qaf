package pw.phylame.qaf.cli

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import pw.phylame.qaf.core.App
import pw.phylame.qaf.core.AppDelegate
import java.util.*

abstract class CLIDelegate : AppDelegate {
    protected val options = Options()

    protected val parser = DefaultParser()

    val context = HashMap<String, Any>()

    protected abstract fun createOptions()

    override fun onStart() {
        createOptions()
    }

    private fun parseOptions() {
        try {
            val cmd = parser.parse(options, App.arguments)
            for (option in cmd.options) {

            }
        } catch (e: ParseException) {
            App.exit(-1)
        }
    }

    private fun dispatchCommands(): Int {
        return 0
    }

    override fun run() {
        parseOptions()
        App.exit(dispatchCommands())
    }
}