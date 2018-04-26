package br.com.ajeferson.client

import br.com.ajeferson.corba.AgendaPOA
import br.com.ajeferson.entity.Contact
import io.reactivex.subjects.PublishSubject

class AgendaClient: AgendaPOA() {

    // TODO GenerateID randomly
    val id = "client0"

    val insertStream: PublishSubject<Contact> = PublishSubject.create()

    override fun isAlive(): Boolean {
        return true
    }

    override fun insert(name: String?, phoneNumber: String?): Boolean {

        if(name == null || phoneNumber == null) {
            return false
        }

        insertStream.onNext(Contact(name, phoneNumber))

        return true

    }

}