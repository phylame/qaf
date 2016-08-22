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

package pw.phylame.qaf.core

import java.io.File
import java.io.InputStream
import java.util.*

const val PLUGIN_CONFIG_KEY = "qaf.config.path"

const val CUSTOMIZED_HOME_KEY = "qaf.home"

interface AppDelegate : Runnable {
    fun onStart() {

    }

    fun onPlugin(plugin: Plugin): Boolean = true

    fun onQuit() {

    }
}

interface Plugin {
    val meta: Map<String, Any>

    fun init()

    fun destroy()
}

object App : Localizable {
    private val _plugins = LinkedList<Plugin>()

    private val _cleanups = LinkedList<Runnable>()

    private val pluginPath by lazy {
        System.getProperty(PLUGIN_CONFIG_KEY) ?: "META-INF/qaf/plugin.prop"
    }

    var assembly: Assembly = Assembly()
        private set

    lateinit var delegate: AppDelegate
        private set

    lateinit var arguments: Array<String>
        private set

    var translator: Localizable? = null

    val plugins: List<Plugin> = _plugins

    val cleanups: List<Runnable> = _cleanups

    val home: String by lazy {
        System.getProperty(CUSTOMIZED_HOME_KEY) ?: System.getProperty("user.home") + File.separatorChar + '.' + assembly.name
    }

    fun ensureHomeExisted() {
        val homeDir = File(home)
        if (!homeDir.exists() && !homeDir.mkdir()) {
            throw RuntimeException("Cannot create home directory: " + home)
        }
    }

    fun addCleanup(cleanup: Runnable) {
        if (cleanup !in _cleanups) {
            _cleanups.add(cleanup)
        }
    }

    fun removeCleanup(cleanup: Runnable) {
        _cleanups.remove(cleanup)
    }

    fun loadPlugins(loader: ClassLoader = Thread.currentThread().contextClassLoader) {
        loader.getResources(pluginPath).asSequence().forEach {
            it.openStream().buffered().use {
                loadPlugin(it)
            }
        }
    }

    fun loadPlugin(input: InputStream) {
        try {
            input.bufferedReader().forEachLine {
                val clazz = Class.forName(it.trim())
                if (Plugin::class.java.isAssignableFrom(clazz)) {
                    val plugin = clazz.newInstance() as Plugin
                    if (delegate.onPlugin(plugin)) {
                        plugin.init()
                        _plugins.add(plugin)
                    }
                }
            }
        } catch (e: ClassNotFoundException) {
        }
    }

    fun pathInHome(name: String): String = home + File.separatorChar + name

    fun echo(msg: Any) {
        System.out.println("${assembly.name}: $msg")
    }

    enum class Debug {
        NONE, ECHO, TRACE
    }

    var debug = Debug.ECHO

    private fun traceback(e: Exception, debug: Debug) {
        when (debug) {
            Debug.ECHO -> println(e.message)
            Debug.TRACE -> e.printStackTrace()
        }
    }

    fun error(msg: Any) {
        System.err.println("${assembly.name}: $msg")
    }

    fun error(msg: Any, e: Exception) {
        error(msg, e, debug)
    }

    fun error(msg: Any, e: Exception, debug: Debug) {
        error(msg)
        traceback(e, debug)
    }

    fun die(msg: Any): Nothing {
        error(msg)
        exit(-1)
    }

    fun die(msg: Any, e: Exception): Nothing {
        die(msg, e, debug)
    }

    fun die(msg: Any, e: Exception, debug: Debug): Nothing {
        error(msg)
        traceback(e, debug)
        exit(-1)
    }

    fun run(name: String, version: String, arguments: Array<String>, delegate: AppDelegate) {
        this.assembly = Assembly(name, version)
        this.arguments = arguments
        this.delegate = delegate
        start()
    }

    fun exit(status: Int = 0): Nothing {
        _plugins.forEach { it.destroy() }
        _cleanups.forEach { it.run() }
        delegate.onQuit()
        System.exit(status)
        // that will never be executed
        throw RuntimeException()
    }

    private fun start() {
        delegate.onStart()
        delegate.run()
    }

    override fun get(key: String): String = translator?.get(key) ?: throw IllegalStateException("No translator specified")
}

fun tr(key: String): String = App.tr(key)

fun tr(key: String, vararg args: Any): String = App.tr(key, *args)
