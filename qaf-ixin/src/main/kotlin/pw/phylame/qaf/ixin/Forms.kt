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

import pw.phylame.qaf.core.Settings
import java.awt.Dimension
import java.util.*
import javax.swing.Action
import javax.swing.JFrame
import javax.swing.JToolBar

open class Form(title: String = "", val snap: Settings? = null) : JFrame(title) {
    private fun createMenuBar() {
    }

    private fun createToolBar() {
    }

    private fun createStatusBar() {
    }

    fun restore() {
    }

    fun destroy() {
    }

    val menuActions = HashMap<String, Action>()

    var toolbar: JToolBar? = null

    companion object {
        const val FORM_LOCATION = "form.location"
        const val FORM_SIZE = "form.size"
        const val TOOL_BAR_VISIBLE = "form.toolbar.visible"
        const val TOOL_BAR_LOCKED = "form.toolbar.locked"
        const val STATUS_BAR_VISIBLE = "form.statusbar.visible"

        const val IXIN_FORM_SIZE = "ixin.form.size"

        val defaultSize by lazy {
            Dimension(780, 439)
        }

    }
}
