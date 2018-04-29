package br.com.ajeferson.client

import br.com.ajeferson.corba.AgendaPOA
import br.com.ajeferson.entity.Contact
import br.com.ajeferson.entity.UpdateContactDto
import io.reactivex.subjects.PublishSubject

class AgendaImpl(val id: String): AgendaPOA() {

    val insertStream: PublishSubject<Contact> = PublishSubject.create()
    val removeStream: PublishSubject<String> = PublishSubject.create()
    val updateStream: PublishSubject<UpdateContactDto> = PublishSubject.create()

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

    override fun remove(name: String?): Boolean {

        if(name == null) {
            return false
        }

        removeStream.onNext(name)

        return true

    }

    override fun update(oldName: String?, newName: String?, newPhoneNumber: String?): Boolean {

        if(oldName == null || newName == null || newPhoneNumber == null) {
            return false
        }

        updateStream.onNext(UpdateContactDto(oldName, Contact(newName, newPhoneNumber)))

        return true

    }

    companion object {
        const val NUMBER_OF_AGENDAS = 3
    }

}