package br.com.ajeferson.controller

import br.com.ajeferson.corba.AgendaPOA
import br.com.ajeferson.entity.Contact

class Agenda(val id: String): AgendaPOA() {

    val contacts = mutableListOf<Contact>()

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

        const val KIND = "Agenda"

    }

}