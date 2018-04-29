package br.com.ajeferson.client

import br.com.ajeferson.client.view.ClientTableModel
import br.com.ajeferson.client.view.SearchResultsView
import br.com.ajeferson.client.viewModel.ClientViewModel
import br.com.ajeferson.client.viewModel.StatusViewModel
import br.com.ajeferson.entity.Contact
import br.com.ajeferson.extension.append
import io.reactivex.subjects.PublishSubject
import java.awt.*
import javax.swing.*

class Client(ip: String, amount: Int): JFrame("Client") {

    private var container: Container = contentPane
    private lateinit var table: JTable
    private lateinit var addBtn: JButton
    private lateinit var removeBtn: JButton
    private lateinit var connectBtn: JButton
    private lateinit var searchBtn: JButton

    // ViewModels
    private var viewModel: ClientViewModel
    private var statusViewModel: StatusViewModel? = null
    set(value) {
        field = value
        if(value != null) {
            statusArea.append(value)
        }
    }
    private lateinit var tableModel: ClientTableModel

    // Streams
    private val contactsStream: PublishSubject<Contact> = PublishSubject.create()
    private val removeStream: PublishSubject<Int> = PublishSubject.create()
    private val searchStream: PublishSubject<String> = PublishSubject.create()
    private val connectStream: PublishSubject<Unit> = PublishSubject.create()


    private lateinit var statusArea: JTextArea


    init {

        size = Dimension(WIDTH, HEIGHT)

        isResizable = false
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        // Status
        container.add(sidePanel, BorderLayout.EAST)
        container.add(bottomPanel, BorderLayout.SOUTH)

        // Init View Model
        viewModel = ClientViewModel(ip, amount, contactsStream, removeStream, connectStream, searchStream)

        // Init Table View
        container.add(tablePane, BorderLayout.CENTER)

        container.repaint()
        isVisible = true
        requestFocus()

        viewModel
                .statusStream
                .subscribe {
                    if(!it.isConnected) {
                        statusViewModel = StatusViewModel(it.description)
                    }
                    addBtn.isEnabled = it.isConnected
                    removeBtn.isEnabled = it.isConnected
                    connectBtn.isEnabled = it.isDisconnected

                }

        viewModel
                .agendaStream
                .subscribe {
                    statusViewModel = StatusViewModel(it)
                }

        viewModel
                .reloadStream
                .subscribe {
                    tableModel.reloadData()
                }

        viewModel
                .errorStream
                .subscribe {
                    statusViewModel = StatusViewModel(it)
                    presentErrorDialog(it)
                }

        viewModel
                .searchResultsStream
                .subscribe {
                    presentSearchResults(it)
                }


        viewModel.init()

    }

    private val bottomPanel: JPanel get()  {

        val panel = JPanel(GridLayout(1, 3))

        // Button Add Contact
        addBtn = JButton("Add Contact")

        addBtn.addActionListener {
            presentInputContactDialog()
        }

        panel.add(addBtn)

        // Remove Contact
        removeBtn = JButton("Remove Contact")
        panel.add(removeBtn)
        removeBtn.addActionListener {
            if(table.selectedRow >= 0) {
                removeStream.onNext(table.selectedRow)
            }
        }

        searchBtn = JButton("Search Contact")
        panel.add(searchBtn)
        searchBtn.addActionListener {
            presentSearchDialog()
        }

        panel.preferredSize = Dimension(0, 35)
        return panel

    }

    private val sidePanel: JPanel get() {

        val panel = JPanel(BorderLayout())

        connectBtn = JButton("Connect")
        connectBtn.addActionListener {
            connectStream.onNext(Unit)
        }
        panel.add(connectBtn, BorderLayout.NORTH)

        // Status area on center
        statusArea = JTextArea()
        statusArea.preferredSize = Dimension(250, 0)
        statusArea.isEditable = false

        val scroll = JScrollPane(statusArea)
        panel.add(scroll)

        return panel

    }

    private val tablePane: JScrollPane get() {

        tableModel = ClientTableModel(viewModel, viewModel)
        table = JTable(tableModel)
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

    private fun presentInputContactDialog() {

        val name = JOptionPane.showInputDialog("Type the contact's name:") ?: return

        if(name.trim().isEmpty()) {
            presentErrorDialog("A contact must have a name")
            return
        }

        val number = JOptionPane.showInputDialog("Type the contact's phone number:") ?: return

        if(number.trim().isEmpty()) {
            presentErrorDialog("A contact must have a phone number")
            return
        }

        val contact = Contact(name, number)
        contactsStream.onNext(contact)

    }

    private fun presentSearchDialog() {

        val input = JOptionPane.showInputDialog("Type your search query:")

        if(input == null || input.isEmpty()) {
            return
        }

        searchStream.onNext(input)

    }

    private fun presentErrorDialog(error: String) {
        JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE)
    }

    private fun presentInfoDialog(info: String) {
        JOptionPane.showMessageDialog(null, info, "Error", JOptionPane.INFORMATION_MESSAGE)
    }

    private fun presentSearchResults(results: List<Contact>) {
        if(results.isNotEmpty()) {
            val searchResultsView = SearchResultsView(results)
            searchResultsView.display()
        } else {
            presentInfoDialog("No contacts found")
        }
    }

    companion object {

        private const val WIDTH = 700
        private const val HEIGHT = 500

    }

}