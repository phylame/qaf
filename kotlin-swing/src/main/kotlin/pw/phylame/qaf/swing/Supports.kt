package pw.phylame.qaf.swing

import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

infix fun Int.x(height: Int): Dimension = Dimension(this, height)

class BlockActionListener(val block: (ActionEvent) -> Unit) : ActionListener {
    override fun actionPerformed(e: ActionEvent) {
        block(e)
    }
}
