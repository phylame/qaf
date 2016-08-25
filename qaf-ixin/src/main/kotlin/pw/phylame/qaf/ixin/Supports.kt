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

object IxinUtils {
    const val MNEMONIC_PREFIX = '&'

    var mnemonicEnable = true

    data class MnemonicResult(
            val name: String,
            val mnemonic: Int,
            val index: Int
    )

    fun splitMnemonic(name: String): MnemonicResult {
        // get mnemonic from name
        var text = name
        var mnemonic = 0

        val index = name.indexOf(MNEMONIC_PREFIX)
        if (index >= 0 && index < name.length) {
            val next = name[index + 1]
            if (next.isLetterOrDigit()) {     // has mnemonic
                mnemonic = next.toInt()
                text = name.substring(0, index) + name.substring(index + 1)
            }
        }
        return MnemonicResult(text, mnemonic, index)
    }

    fun trimMnemonic(text: String, mnemonicIndex: Int, bracketLength: Int = 1): String {
        if (mnemonicIndex == -1 || bracketLength == 0) {
            return text
        }
        return text.substring(0, mnemonicIndex - bracketLength) + text.substring(mnemonicIndex + 1 + bracketLength)
    }

}
