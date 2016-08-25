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

package pw.phylame.qaf.ixin

import pw.phylame.qaf.core.AppDelegate
import pw.phylame.qaf.core.Plugin
import java.util.*
import javax.swing.SwingUtilities

interface IPlugin : Plugin {
    /**
     * Executed after initializing UI components.
     */
    fun performUI()
}

abstract class IxinDelegate<F : Form> : AppDelegate, CommandListener {

    lateinit var resource: Resource
        protected set

    lateinit var proxy: CommandListener
        protected set

    lateinit var form: F
        private set

    abstract fun createForm(): F

    override fun commandPerformed(command: String) {
        // forward to proxy
        proxy.commandPerformed(command)
    }

    override fun onPlugin(plugin: Plugin): Boolean {
        if (plugin is IPlugin) {
            plugins.add(plugin)
        }
        return super.onPlugin(plugin)
    }

    override final fun run() {
        SwingUtilities.invokeLater { initUI() }
    }

    private fun initUI() {
        form = createForm()
        plugins.forEach { it.performUI() }
    }

    override fun onQuit() {
        form.destroy()
        super.onQuit()
    }

    private val plugins = LinkedHashSet<IPlugin>()
}
