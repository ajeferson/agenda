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

    private fun agendaId(number: Int) = "agenda$number"

    fun connect() {

        var i = 0

        while (i < NUMBER_OF_AGENDAS && agenda == null) {

            i++

            try {
                val name = arrayOf(NameComponent(agendaId(i), Agenda.KIND))
                val objRef = namingContext.resolve(name)
                agenda = AgendaHelper.narrow(objRef)
                agenda?.isAlive
            } catch (e: Exception) {
                agenda = null
            }

        }

        if(agenda != null) {
            println("Connected to ${agendaId(i)}")
        } else {
            println("Could not connect to any of the agendas :(")
        }

    }



    /**
     * Contact methods
     * */


    companion object {

        private const val NUMBER_OF_AGENDAS = 3

    }

}

fun main(args: Array<String>) {
    val client = Client(args)
    client.connect()
    client.agenda?.insert("Kiko", "123456")
}