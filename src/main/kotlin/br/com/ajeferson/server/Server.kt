package br.com.ajeferson.server

import br.com.ajeferson.client.AgendaImpl
import br.com.ajeferson.corba.AgendaHelper
import br.com.ajeferson.corba.IdentityManagerHelper
import br.com.ajeferson.corba.IdentityManagerPOA
import br.com.ajeferson.entity.Contact
import br.com.ajeferson.enumeration.AgendaKind
import br.com.ajeferson.extension.disposedBy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.omg.CORBA.ORB
import org.omg.CosNaming.NameComponent
import org.omg.CosNaming.NamingContext
import org.omg.CosNaming.NamingContextHelper
import org.omg.PortableServer.POA
import org.omg.PortableServer.POAHelper

class Server(args: Array<String>): IdentityManagerPOA() {

    private val clients = mutableListOf<br.com.ajeferson.corba.Agenda>()
    private lateinit var agenda: AgendaImpl

    private lateinit var rootPoa: POA
    private lateinit var namingContext: NamingContext

    private val disposables = CompositeDisposable()

    private val contacts = mutableListOf<Contact>()

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
            agenda = AgendaImpl(args[2])
            val objRef = rootPoa.servant_to_reference(agenda)
            val components = arrayOf(NameComponent(agenda.id, AgendaKind.AGENDA.description))
            namingContext.rebind(components, objRef)

            // Bind IdentityManager
            val ref = rootPoa.servant_to_reference(this)
            val comp = arrayOf(NameComponent(args[2], IDENTITY_MANAGER_KIND))
            namingContext.rebind(comp, ref)

            rootPoa.the_POAManager().activate()

            println("Agenda ${agenda.id} is ready")

            observe()

            connect()

            orb.run()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observe() {

        agenda
                .insertStream
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe {
                    didReceiveContact(it)
                }
                .disposedBy(disposables)

        agenda
                .removeStream
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe {
                    didRemoveContact(it)
                }
                .disposedBy(disposables)

    }

    private fun connect() {

        var id = 0
        var sync = true

        while (id < AgendaImpl.NUMBER_OF_AGENDAS) {

            id++

            if("agenda$id" == agenda.id) {
                continue
            }

            try {

                // Get the AgendaServer
                val name = arrayOf(NameComponent("agenda$id", IDENTITY_MANAGER_KIND))
                val objRef = namingContext.resolve(name)
                val identityManager = IdentityManagerHelper.narrow(objRef)

                // This triggers the sending of contacts by other agenda
                identityManager.identify(agenda.id, sync)
                sync = false

            } catch (e: Exception) {

            }

        }

    }


    /**
     * IdentityManager Implement
     * */

    override fun identify(identity: String?, sync: Boolean) {

        if(identity == null) {
            return
        }

        val name = arrayOf(NameComponent(identity, AgendaKind.AGENDA.description))
        val ref = namingContext.resolve(name)

        val client = AgendaHelper.narrow(ref)
        clients.add(client)

        if(!sync) { return }

        // Send all my data to this new client
        contacts.forEach { contact ->
            try {
                client.insert(contact.name, contact.phoneNumber)
            } catch (e: Exception) {

            }
        }

    }



    /**
     * Contacts
     * */

    private fun didReceiveContact(contact: Contact) {

        contacts.add(contact)

        clients.forEach { agenda ->
            try {
                agenda.insert(contact.name, contact.phoneNumber)
            } catch (e: Exception) {

            }
        }

    }

    private fun didRemoveContact(name: String) {

        contacts.removeIf { it.name == name }

        clients.forEach { agenda ->
            try {
                agenda.remove(name)
            } catch (e: Exception) {

            }
        }

    }

    companion object {

        const val IDENTITY_MANAGER_KIND = "IdentityManager"

    }

}

fun main(args: Array<String>) {
    Server(args)
}