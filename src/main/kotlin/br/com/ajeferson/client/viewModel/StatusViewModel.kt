package br.com.ajeferson.client.viewModel

import java.text.SimpleDateFormat
import java.util.*

data class StatusViewModel(private val log: String) {

    private val date = Date()

    private val dateFormatter: SimpleDateFormat by lazy {
        val sdf = SimpleDateFormat("HH:mm:ss")
        sdf
    }

    private val formattedDate: String
        get() = "[${dateFormatter.format(date)}]"

    val description: String
        get() = "$formattedDate $log"

}