import pw.phylame.qaf.ixin.Ixin
import pw.phylame.qaf.ixin.title
import pw.phylame.qaf.ixin.x
import pw.phylame.qaf.swing.*
import java.awt.Font
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

fun main(args: Array<String>) {
    Ixin.init(true, false, "Nimbus", Font("Consolas", Font.PLAIN, 16))
    val frame = frame {
        size = 800 x 450
        title = "This is a pw.phylame.qaf.swing.frame"
        setLocationRelativeTo(null)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                System.exit(0)
            }
        })

        menuBar {
            menu {
                title = "&File"

                item {
                    text = "New"
                }

                item {
                    text = "Open"
                }

                item {
                    text = "Save"
                }

                separator()

                item {
                    text = "Exit"

                    addActionListener {
                        System.exit(0)
                    }
                }
            }
            menu {
                text = "Edit"

                item {
                    text = "Copy"
                }

                item {
                    text = "Paste"
                }

                separator()

                menu {
                    text = "Find"

                    item {
                        text = "Find"
                    }

                    item {
                        text = "Search"
                    }
                }
            }

            menu {
                title = "&View"

                radio {
                    text = "Haha"
                }

                check {
                    text = "Check"
                }

                separator()

                buttonGroup {
                    radio {
                        text = "10 pt"
                    }

                    radio {
                        text = "15 pt"
                        isSelected = true
                    }

                    radio {
                        text = "20 pt"
                    }
                }
            }

            menu {
                text = "Help"

                item {
                    text = "Help"
                }

                item {
                    text = "About"
                }
            }
        }

        borderLayout {
            north = toolBar(false) {
                isRollover = true
                isFloatable = false

                button {
                    text = "New"
                }

                button {
                    text = "Open"
                }

                button {
                    text = "Save"
                }

                separator()

                buttonGroup {
                    radioButton {
                        title = "&One"
                        it.add(this)
                    }

                    radioButton {
                        title = "&Two"
                        it.add(this)
                    }

                    radioButton {
                        title = "T&hree"
                        it.add(this)
                    }
                }

                separator()

                radioButton {
                    title = "&Another"
                }

                separator()

                textField {
                    columns = 16
                    toolTipText = "Go To"
                }

                button {
                    text = "Go"
                }
            }
            center = pane(false) {
                borderLayout {
                    center = splitPane(false) {
                        leftComponent = pane(false) {
                            borderLayout {
                                north = pane(false) {
                                    borderLayout {
                                        west = label(false) {
                                            text = "Contents"
                                            icon = ImageIcon(javaClass.getResource("/info.png"))
                                        }

                                        east = toolBar(false) {
                                            isRollover = true
                                            isFloatable = false
                                            isBorderPainted = false

                                            button {
                                                icon = ImageIcon(javaClass.getResource("/info.png"))
                                            }

                                            button {
                                                icon = ImageIcon(javaClass.getResource("/info.png"))
                                            }
                                        }
                                    }
                                }

                                center = scrollPane(false) {
                                    content = JTree().apply {

                                    }
                                }
                            }
                        }

                        rightComponent = tabbedPane(false) {
                            tab("Environments") {
                                scrollPane(false) {
                                    textArea {
                                        lineWrap = true
                                        wrapStyleWord = true
                                        text = System.getenv()
                                                .entries
                                                .joinToString("\n", "System Environments:\n----------------\n") {
                                                    "%32s: %s".format(it.key, it.value)
                                                }
                                    }
                                }
                            }

                            tab("Two") {
                                JTable()
                            }

                            tab("Properties") {
                                scrollPane(false) {
                                    textArea {
                                        lineWrap = true
                                        wrapStyleWord = true
                                        text = System.getProperties()
                                                .entries
                                                .joinToString("\n", "System Properties:\n----------------\n") {
                                                    "%32s: %s".format(it.key, it.value)
                                                }
                                    }
                                }
                            }

                            tab("Label") {
                                label(false) {
                                    verticalAlignment = SwingConstants.TOP
                                    horizontalAlignment = SwingConstants.CENTER
                                    text = "This is a label"
                                }
                            }
                        }
                    }
                }
            }
            south = pane(false) {
                borderLayout {
                    north = JSeparator()
                    west = label(false) {
                        text = "Ready"
                    }
                }
            }
        }
    }

    frame.isVisible = true
}
