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

import pw.phylame.commons.io.IOUtils
import pw.phylame.commons.io.TextCache.TAG
import pw.phylame.commons.log.Log
import java.io.File
import java.io.InputStream
import java.util.*

const val HOME_PATH_KEY = "qaf.home"
const val PLUGIN_CONFIG_KEY = "qaf.config"
const val DEFAULT_CONFIG_PATH = "META-INF/qaf/plugin.prop"

/**
 * The delegate for app workflow, when creating the delegate instance, the methods of App is inaccessible.
 */
interface AppDelegate : Runnable {
    /**
     * Initializes the app before running.
     */
    fun onStart() {}

    /**
     * Filters the specified plugin.
     */
    fun onPlugin(plugin: Plugin): Boolean = true

    /**
     * Destroys the app before exit.
     */
    fun onQuit() {
        App.plugins.values.forEach(Plugin::destroy)
        App.cleanups.forEach(Runnable::run)
    }
}

enum class Verbose {
    NONE, ECHO, TRACE
}

enum class State {
    STARTING, RUNNING, STOPPING, STOPPED
}

object App : LocalizableWrapper() {
    lateinit var assembly: Assembly
        private set

    lateinit var delegate: AppDelegate
        private set

    lateinit var arguments: Array<String>
        private set

    val cleanups = LinkedHashSet<Runnable>()

    val plugins = LinkedHashMap<String, Plugin>()

    var code: Int = 0
        private set

    var state = State.STARTING
        private set

    val home by lazy {
        System.getProperty(HOME_PATH_KEY) or "${System.getProperty("user.home")}/.${assembly.name}"
    }

    fun ensureHomeExists() {
        val dir = File(home)
        if (!dir.exists() && !dir.mkdirs()) {
            throw RuntimeException(optTr("qaf.err.createHome", "Cannot create home directory: {0}", dir))
        }
    }

    fun pathOf(name: String) = "$home/$name"

    fun fileOf(name: String) = File(pathOf(name))

    fun loadPlugins(blacklist: Set<String> = emptySet(), loader: ClassLoader? = null) {
        IOUtils.resourcesFor(System.getProperty(PLUGIN_CONFIG_KEY, DEFAULT_CONFIG_PATH), loader).forEach {
            it.openStream().buffered().use {
                loadPlugin(it, blacklist, loader)
            }
        }
    }

    fun loadPlugin(input: InputStream, blacklist: Set<String> = emptySet(), loader: ClassLoader? = null) {
        input.bufferedReader()
                .lineSequence()
                .map(String::trim)
                .filter { it.isNotBlank() && !it.startsWith('#') && it !in blacklist }
                .forEach { path ->
                    try {
                        val clazz = loader?.loadClass(path) ?: Class.forName(path)
                        if (Plugin::class.java.isAssignableFrom(clazz)) {
                            val plugin = clazz.newInstance() as Plugin
                            if (delegate.onPlugin(plugin)) {
                                plugin.init()
                                plugins[plugin.uuid] = plugin
                            }
                        } else {
                            Log.e(TAG, "class $path is not instance of ${Plugin::class.java.name}")
                        }
                    } catch (e: ClassNotFoundException) {
                        Log.e(TAG, "not found plugin in \"$path\"")
                    } catch (e: Throwable) {
                        Log.e(TAG, "unknown error", e)
                    }
                }
    }

    fun run(name: String, version: String, delegate: AppDelegate, arguments: Array<String>) {
        state = State.STARTING
        this.assembly = Assembly(name, version)
        this.arguments = arguments
        this.delegate = delegate
        delegate.onStart()
        state = State.RUNNING
        delegate.run()
        exit(0)
    }

    fun exit(status: Int = 0): Nothing {
        code = status
        state = State.STOPPING
        delegate.onQuit()
        state = State.STOPPED
        System.exit(status)
        throw InternalError()
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
