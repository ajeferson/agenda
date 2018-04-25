package br.com.ajeferson.controller

import org.omg.CORBA.ORB
import org.omg.CosNaming.NameComponent
import org.omg.CosNaming.NamingContextHelper
import org.omg.PortableServer.POAHelper

fun main(args: Array<String>) {

    if(args.size < 3) {
        throw IllegalArgumentException("Provide the Server id")
    }

    try {

        val orb = ORB.init(args, null)

        val objPoa = orb.resolve_initial_references("RootPOA")
        val obj = orb.resolve_initial_references("NameService")

        val rootPoa = POAHelper.narrow(objPoa)
        val naming = NamingContextHelper.narrow(obj)

        val agenda = Agenda(args[2])
        val objRef = rootPoa.servant_to_reference(agenda)

        val components = arrayOf(NameComponent(agenda.id, Agenda.KIND))
        naming.rebind(components, objRef)

        rootPoa.the_POAManager().activate()

        println("Agenda ${agenda.id} is ready")

        orb.run()

    } catch (e: Exception) {
        e.printStackTrace()
    }

}