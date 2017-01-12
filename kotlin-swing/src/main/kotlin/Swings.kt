import javax.swing.JFrame

fun frame(init: JFrame.() -> Unit): JFrame {
    val frame = JFrame()
    frame.init()
    return frame
}
