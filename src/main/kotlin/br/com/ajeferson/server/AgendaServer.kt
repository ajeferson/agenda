package br.com.ajeferson.server

import br.com.ajeferson.corba.AgendaPOA
import br.com.ajeferson.entity.Contact
import io.reactivex.subjects.PublishSubject

class AgendaServer(val id: String): AgendaPOA() {

    val contacts = mutableListOf<Contact>()

    val insertStream: PublishSubject<Contact> = PublishSubject.create()

    override fun isAlive(): Boolean {
        return true
    }

    override fun insert(name: String?, phoneNumber: String?): Boolean {
        if(name == null || phoneNumber == null) {
            return false
        }
        println("Inserting: $name $phoneNumber")
        return true
    }

    companion object {

        const val KIND = "AgendaServer"

    }

}