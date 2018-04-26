package br.com.ajeferson.client

import br.com.ajeferson.client.protocol.TableDataSource
import br.com.ajeferson.entity.Contact
import io.reactivex.Observable

class ClientViewModel(contactsStream: Observable<Contact>): TableDataSource {

    private val contacts = mutableListOf<Contact>()
    private val columns = listOf("Name", "Phone Number")


    init {
        contactsStream
                .subscribe {
                    print("New Contact: $it")
                }
    }


    /**
     * Table Data Source
     * */

    override fun numberOfRows()= contacts.size

    override fun numberOfColumns() = columns.size

    override fun columnNameAt(index: Int) = columns[index]

    override fun valueAt(row: Int, column: Int) = when(column) {
        0 -> contacts[row].name
        else -> contacts[row].phoneNumber
    }

}