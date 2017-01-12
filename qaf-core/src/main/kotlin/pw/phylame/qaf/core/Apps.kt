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

import pw.phylame.ycl.io.IOUtils
import pw.phylame.ycl.log.Log
import java.io.File
import java.io.InputStream
import java.util.*

const val PLUGIN_CONFIG_KEY = "qaf.config.path"
const val DEFAULT_CONFIG_PATH = "META-INF/qaf/plugin.prop"
const val CUSTOMIZED_HOME_KEY = "qaf.home.path"

interface Plugin {
    val id: String

    val meta: Map<String, Any>

    fun init()

    fun destroy()
}

data class Metadata(val id: String, val name: String, val version: String, val vendor: String) {
    fun toMap(): Map<String, Any> {
        return mapOf("name" to name, "version" to version, "vendor" to vendor)
    }
}

abstract class AbstractPlugin(private val metadata: Metadata) : Plugin {
    val app = App

    override val id: String get() = metadata.id

    override val meta: Map<String, Any> get() = metadata.toMap()

    override fun destroy() {

    }

    override fun toString(): String = "${javaClass.simpleName}[id=$id, meta=$meta]"
}

/**
 * The delegate for app workflow, when creating the delegate instance, the methods of App is inaccessible.
 */
interface AppDelegate : Runnable {
    /**
     * Does initialing tasks for the app.
     */
    fun onStart() {

    }

    /**
     * Filters the specified plugin.
     */
    fun onPlugin(plugin: Plugin): Boolean = true

    /**
     * Does tasks when quitting app.
     */
    fun onQuit() {
        App.plugins.values.forEach(Plugin::destroy)
        App.cleanups.forEach(Runnable::run)
    }
}

enum class DebugLevel {
    NONE, ECHO, TRACE
}

object App : LocalizableWrapper() {
    private const val TAG = "App"

    val plugins = LinkedHashMap<String, Plugin>()

    val cleanups = LinkedHashSet<Runnable>()

    lateinit var assembly: Assembly
        private set

    lateinit var delegate: AppDelegate
        private set

    lateinit var arguments: Array<String>
        private set

    val home: String by lazy {
        (System.getProperty(CUSTOMIZED_HOME_KEY) ?: System.getProperty("user.home")) + File.separatorChar + '.' + assembly.name
    }

    fun ensureHomeExisted() {
        val dir = File(home)
        if (!dir.exists() && !dir.mkdir()) {
            throw RuntimeException("Cannot create home directory: \"$home\"")
        }
    }

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
                                plugins[plugin.id] = plugin
                            }
                        } else {
                            Log.e(TAG, "class $path is not instance of ${Plugin::class.java.name}")
                        }
                    } catch (e: ClassNotFoundException) {
                        Log.e(TAG, "not found plugin in \"$path\"")
                    } catch (e: Throwable) {
                        Log.e(TAG, e)
                    }
                }
    }

    fun pathOf(name: String): String = home + File.separatorChar + name

    var debugLevel = DebugLevel.ECHO

    fun echo(msg: Any) {
        System.out.println("${assembly.name}: $msg")
    }

    fun error(msg: Any) {
        System.err.println("${assembly.name}: $msg")
    }

    fun error(msg: Any, e: Throwable) {
        error(msg, e, debugLevel)
    }

    fun error(msg: Any, e: Throwable, level: DebugLevel) {
        error(msg)
        traceback(e, level)
    }

    fun die(msg: Any): Nothing {
        error(msg)
        exit(-1)
    }

    fun die(msg: Any, e: Throwable): Nothing {
        die(msg, e, debugLevel)
    }

    fun die(msg: Any, e: Throwable, level: DebugLevel): Nothing {
        error(msg)
        traceback(e, level)
        exit(-1)
    }

    private fun traceback(e: Throwable, level: DebugLevel) {
        when (level) {
            DebugLevel.ECHO -> System.out.println("  ${e.message}")
            DebugLevel.TRACE -> e.printStackTrace()
            else -> Unit
        }
    }

    fun run(name: String, version: String, delegate: AppDelegate, arguments: Array<String>) {
        this.assembly = Assembly(name, version)
        this.delegate = delegate
        this.arguments = arguments
        start()
    }

    fun exit(status: Int = 0): Nothing {
        delegate.onQuit()
        System.exit(status)
        // that will never be executed
        throw InternalError()
    }

    private fun start() {
        delegate.onStart()
        delegate.run()
    }
}

fun tr(key: String): String = App.tr(key)

fun optTr(key: String, default: String): String = App.optTr(key, default)

fun tr(key: String, vararg args: Any?): String = App.tr(key, *args)

fun optTr(key: String, default: String, vararg args: Any?): String = App.optTr(key, default, *args)
