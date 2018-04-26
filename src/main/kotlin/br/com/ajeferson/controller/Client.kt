package br.com.ajeferson.controller

import br.com.ajeferson.corba.AgendaHelper
import org.omg.CORBA.ORB
import org.omg.CosNaming.NameComponent
import org.omg.CosNaming.NamingContext
import org.omg.CosNaming.NamingContextHelper

class Client(args: Array<String>) {

    private val namingContext: NamingContext
    var agenda: br.com.ajeferson.corba.Agenda? = null

    init {

        val orb = ORB.init(args, null)
        val obj = orb.resolve_initial_references("NameService")

        namingContext = NamingContextHelper.narrow(obj)

    }







//    /**
//     * Contact methods
//     * */
//
//
//    companion object {
//
//
//
//    }

}

fun main(args: Array<String>) {
//    val client = Client(args)
//    client.connect()
//    client.agenda?.insert("Kiko", "123456")
}