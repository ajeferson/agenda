package br.com.ajeferson.client

import br.com.ajeferson.entity.Contact
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.awt.*
import javax.swing.*

class ClientView: JFrame("Client") {

    private var container: Container = contentPane

    private var viewModel: ClientViewModel

    // Streams
    private val contactsStream: PublishSubject<Contact> = PublishSubject.create<Contact>()


    init {

        size = Dimension(WIDTH, HEIGHT)

        isResizable = false
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        // Add bottom buttons
        container.add(bottomPanel, BorderLayout.SOUTH)

        // Init View Model
        viewModel = ClientViewModel(contactsStream)

        // Init Table View
        container.add(tablePane, BorderLayout.CENTER)

        container.repaint()
        isVisible = true
        requestFocus()

    }

    private val bottomPanel: JPanel get()  {

        val panel = JPanel(GridLayout(1, 2))
        panel.background = Color.RED

        // Button Add Contact
        val addBtn = JButton("Add Contact")

        Observable.create<Unit> { emitter ->
            addBtn.addActionListener {
                emitter.onNext(Unit)
            }
        }

        panel.add(addBtn)

        // Remove Contact
        val removeBtn = JButton("Remove Contact")
        panel.add(removeBtn)

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

        private const val WIDTH = 500
        private const val HEIGHT = 500

    }

}

fun main(args: Array<String>) {

    SwingUtilities.invokeLater { ClientView() }

}