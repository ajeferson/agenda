package br.com.ajeferson.client.view

import br.com.ajeferson.entity.Contact
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea

class SearchResultsView(private val results: List<Contact>): JFrame() {

    private lateinit var textArea: JTextArea

    init {

        size = Dimension(300, 250)
        title = "Search Results"
        isResizable = false

        initTextArea()
        presentResults()

    }

    private fun initTextArea() {
        textArea = JTextArea()
        textArea.isEditable = false
        val scroll = JScrollPane(textArea)
        contentPane.add(scroll)
    }

    private fun presentResults() {
        results.forEach {
            textArea.append("Name: ${it.name}\nPhone number: ${it.phoneNumber}\n\n")
        }
    }

    fun display() {
        isVisible = true
    }

}