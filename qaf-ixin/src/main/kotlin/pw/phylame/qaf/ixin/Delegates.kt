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
import javax.swing.SwingUtilities

abstract class IxinDelegate<F : Form> : AppDelegate {
    companion object {

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onPlugin(plugin: Plugin): Boolean {
        return super.onPlugin(plugin)
    }

    override final fun run() {
        SwingUtilities.invokeLater { initUI() }
    }

    private fun initUI() {
        form = createForm()
    }

    override fun onQuit() {
        form.destroy()
    }

    lateinit var form: F
        private set

    abstract fun createForm(): F
}
