package test

import pw.phylame.qaf.cli.CLIDelegate
import pw.phylame.qaf.core.App

object MyDelegate : CLIDelegate() {
    override fun createOptions() {
        println("createOptions")
    }
}

fun main(args: Array<String>) {
    App.run("cli-demo", "1.0", MyDelegate, args)
}