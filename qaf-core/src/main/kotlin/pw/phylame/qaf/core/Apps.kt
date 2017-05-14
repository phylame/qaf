/*
 * Copyright 2015-2016 Peng Wan <phylame@163.com>
 *
 * This file is part of Qaf.
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

package pw.phylame.qaf.core

import java.io.File
import java.util.*

const val HOME_PATH_KEY = "qaf.home"

/**
 * The delegate for app workflow, when creating the delegate instance, the methods of App is inaccessible.
 */
interface AppDelegate : Runnable {
    /**
     * Initializes the app before running.
     */
    fun onStart() {}

    /**
     * Destroys the app before exit.
     */
    fun onQuit() {
        App.cleanups.forEach(Runnable::run)
    }
}

enum class Verbose {
    NONE, ECHO, TRACE
}

object App : LocalizableWrapper() {
    lateinit var assembly: Assembly
        private set

    lateinit var delegate: AppDelegate
        private set

    lateinit var arguments: Array<String>
        private set

    internal var isInitialized: Boolean = false
        private set

    val cleanups = LinkedHashSet<Runnable>()

    val home by lazy {
        System.getProperty(HOME_PATH_KEY) or "${System.getProperty("user.home")}/.${assembly.name}"
    }

    fun pathOf(name: String) = "$home/$name"

    fun fileOf(name: String) = File(pathOf(name))

    fun run(name: String, version: String, delegate: AppDelegate, arguments: Array<String>) {
        this.assembly = Assembly(name, version)
        this.arguments = arguments
        this.delegate = delegate
        isInitialized = true
        delegate.onStart()
        delegate.run()
    }

    fun exit(status: Int = 0): Nothing {
        delegate.onQuit()
        System.exit(status)
        throw RuntimeException()
    }

    var verbose = Verbose.ECHO

    fun echo(msg: Any) {
        System.out.println("${assembly.name}: $msg")
    }

    fun error(msg: Any) {
        System.err.println("${assembly.name}: $msg")
    }

    fun error(msg: Any, e: Throwable) {
        error(msg)
        traceback(e, verbose)
    }

    fun error(msg: Any, e: Throwable, level: Verbose) {
        error(msg)
        traceback(e, level)
    }

    fun die(msg: Any): Nothing {
        error(msg)
        exit(-1)
    }

    fun die(msg: Any, e: Throwable): Nothing {
        error(msg, e)
        exit(-1)
    }

    fun die(msg: Any, e: Throwable, level: Verbose): Nothing {
        error(msg, e, level)
        exit(-1)
    }

    private fun traceback(e: Throwable, level: Verbose) {
        when (level) {
            Verbose.ECHO -> System.out.println(" ${e.message}")
            Verbose.TRACE -> e.printStackTrace()
            else -> Unit
        }
    }
}
