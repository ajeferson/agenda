package br.com.ajeferson.server

import br.com.ajeferson.corba.AgendaHelper
import br.com.ajeferson.corba.IdentityManagerPOA
import org.omg.CORBA.ORB
import org.omg.CosNaming.NameComponent
import org.omg.CosNaming.NamingContext
import org.omg.CosNaming.NamingContextHelper
import org.omg.PortableServer.POA
import org.omg.PortableServer.POAHelper

class Server(args: Array<String>): IdentityManagerPOA() {

    private val clients = mutableListOf<br.com.ajeferson.corba.Agenda>()

    private lateinit var rootPoa: POA
    private lateinit var namingContext: NamingContext

    init {

        if(args.size < 3) {
            throw IllegalArgumentException("Provide the Server id")
        }

        try {

            val orb = ORB.init(args, null)

            val objPoa = orb.resolve_initial_references("RootPOA")
            val obj = orb.resolve_initial_references("NameService")

            rootPoa = POAHelper.narrow(objPoa)
            namingContext = NamingContextHelper.narrow(obj)

            // Bind AgendaServer
            val agenda = AgendaServer(args[2])
            val objRef = rootPoa.servant_to_reference(agenda)
            val components = arrayOf(NameComponent(agenda.id, AgendaServer.KIND))
            namingContext.rebind(components, objRef)

            // Bind IdentityManager
            val ref = rootPoa.servant_to_reference(this)
            val comp = arrayOf(NameComponent(args[2], IDENTITY_MANAGER_KIND))
            namingContext.rebind(comp, ref)

            rootPoa.the_POAManager().activate()

            println("Agenda ${agenda.id} is ready")

            orb.run()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    /**
     * IdentityManager Implement
     * */

    override fun identify(identity: String?) {

        if(identity == null) {
            return
        }

        val name = arrayOf(NameComponent(identity, AgendaServer.KIND))
        val ref = namingContext.resolve(name)
        clients.add(AgendaHelper.narrow(ref))

    }

    companion object {

        const val IDENTITY_MANAGER_KIND = "IdentityManager"

    }

}

fun main(args: Array<String>) {
    Server(args)
}