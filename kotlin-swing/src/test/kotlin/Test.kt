import pw.phylame.qaf.ixin.Ixin
import pw.phylame.qaf.swing.*
import java.awt.Font
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

fun main(args: Array<String>) {
    Ixin.init(true, false, "Nimbus", Font("Microsoft YaHei UI", Font.PLAIN, 14))
    val frame = frame {
        title = "This is a pw.phylame.qaf.swing.frame"
        size = 800 x 450

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                System.exit(0)
            }
        })

        menuBar {
            menu {
                text = "File"

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

                    actionListener {
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
                button {
                    text = "New"
                }

                button {
                    text = "Open"
                }

                button {
                    text = "Save"
                }
            }
            center = panel(false) {
                border = BorderFactory.createEtchedBorder()

                borderLayout {
                    center = splitPane(false) {
                        leftComponent = panel(false) {
                            borderLayout {
                                north = panel(false) {
                                    borderLayout {
                                        west = label {
                                            text = "Contents"
                                            icon = ImageIcon(javaClass.getResource("/info.png"))
                                        }

                                        east = toolBar(false) {
                                            isRollover = true
                                            isFloatable = false

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
                                    setViewportView(JTree())
                                }
                            }
                        }

                        rightComponent = tabbedPane(false) {
                            addTab("One", panel(false) {
                                borderLayout {
                                    center = JTextArea()
                                }
                            })

                            addTab("Two", panel(false) {
                                borderLayout {
                                    center = JTable()
                                }
                            })

                            addTab("Three", panel(false) {
                                borderLayout {
                                    center = JTextArea()
                                }
                            })
                        }
                    }
                }
            }
            south = panel(false) {
                border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
                borderLayout {
                    west = label(false) {
                        text = "Ready"
                    }
                }
            }
        }
    }

    frame.isVisible = true
}
