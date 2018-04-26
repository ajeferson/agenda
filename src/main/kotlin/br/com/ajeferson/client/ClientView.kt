package br.com.ajeferson.client

import br.com.ajeferson.entity.Contact
import br.com.ajeferson.extension.append
import io.reactivex.subjects.PublishSubject
import java.awt.*
import javax.swing.*

class ClientView: JFrame("Client") {

    private var container: Container = contentPane

    private var viewModel: ClientViewModel

    private var statusViewModel: StatusViewModel? = null
    set(value) {
        field = value
        if(value != null) {
            statusArea.append(value)
        }
    }

    // Streams
    private val contactsStream: PublishSubject<Contact> = PublishSubject.create()


    private lateinit var statusArea: JTextArea


    init {

        size = Dimension(WIDTH, HEIGHT)

        isResizable = false
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        // Status
        container.add(sidePanel, BorderLayout.EAST)
        container.add(bottomPanel, BorderLayout.SOUTH)

        // Init View Model
        viewModel = ClientViewModel(contactsStream)

        // Init Table View
        container.add(tablePane, BorderLayout.CENTER)

        container.repaint()
        isVisible = true
        requestFocus()

        viewModel
                .statusStream
                .subscribe {
                    statusViewModel = StatusViewModel(it.description)
                }

        viewModel
                .agendaStream
                .subscribe {
                    statusViewModel = StatusViewModel(it)
                }

        viewModel.init()

    }

    private val bottomPanel: JPanel get()  {

        val panel = JPanel(GridLayout(1, 2))
        panel.background = Color.RED

        // Button Add Contact
        val addBtn = JButton("Add Contact")

        addBtn.addActionListener {

        }

        panel.add(addBtn)

        // Remove Contact
        val removeBtn = JButton("Remove Contact")
        panel.add(removeBtn)

        panel.preferredSize = Dimension(0, 35)
        return panel

    }

    private val sidePanel: JPanel get() {

        val panel = JPanel(BorderLayout())

        // Status area on center
        statusArea = JTextArea()
        statusArea.preferredSize = Dimension(250, 0)
        statusArea.isEditable = false

        val scroll = JScrollPane(statusArea)
        panel.add(scroll)

        return panel

    }

    private val tablePane: JScrollPane get() {

        val table = JTable(ClientTableModel(viewModel))
        val scroll = JScrollPane(table)

        table.fillsViewportHeight = true

        // Columns Widths
        val namesWidth = (0.6 * WIDTH).toInt()
        table.columnModel.getColumn(0).preferredWidth = namesWidth
        table.columnModel.getColumn(1).preferredWidth = WIDTH - namesWidth

        // Allow single selection
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        // Can select only an entire row
        table.rowSelectionAllowed = true
        table.columnSelectionAllowed = false

        return scroll

    }

    companion object {

        private const val WIDTH = 700
        private const val HEIGHT = 500

    }

}

fun main(args: Array<String>) {

    SwingUtilities.invokeLater { ClientView() }

}