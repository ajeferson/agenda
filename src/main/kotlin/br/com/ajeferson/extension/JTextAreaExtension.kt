package br.com.ajeferson.extension

import br.com.ajeferson.client.StatusViewModel
import javax.swing.JTextArea

fun JTextArea.lineBreakAppend(text: String) {
    append("$text\n")
}

fun JTextArea.append(viewModel: StatusViewModel) {
    lineBreakAppend(viewModel.description)
}