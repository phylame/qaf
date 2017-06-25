import qaf.ixin.addAlignedComponents
import qaf.ixin.x
import java.awt.Component
import javax.swing.*

fun main(args: Array<String>) {
    val frame = JFrame()
    addAlignedComponents(frame.contentPane as JPanel, SwingConstants.TOP, BoxLayout.PAGE_AXIS, 5,
            JLabel("Haha").apply {
                alignmentX = Component.CENTER_ALIGNMENT
            }, JButton("Ok").apply {
        alignmentX = Component.CENTER_ALIGNMENT
    }, JCheckBox("Yes").apply {
        alignmentX = Component.CENTER_ALIGNMENT
    }, Box.createVerticalGlue())
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.size = 800 x 600
    frame.isVisible = true
}
