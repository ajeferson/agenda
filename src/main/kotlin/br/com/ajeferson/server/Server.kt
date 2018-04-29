package br.com.ajeferson.server

import br.com.ajeferson.agenda.AgendaImpl
import br.com.ajeferson.corba.Agenda
import br.com.ajeferson.corba.AgendaHelper
import br.com.ajeferson.corba.IdentityManagerHelper
import br.com.ajeferson.corba.IdentityManagerPOA
import br.com.ajeferson.entity.Contact
import br.com.ajeferson.entity.UpdateContactDto
import br.com.ajeferson.enumeration.AgendaKind
import br.com.ajeferson.extension.disposedBy
import io.reactivex.disposables.CompositeDisposable
import org.omg.CORBA.ORB
import org.omg.CosNaming.NameComponent
import org.omg.CosNaming.NamingContext
import org.omg.CosNaming.NamingContextHelper
import org.omg.PortableServer.POA
import org.omg.PortableServer.POAHelper

class Server(id: String, ip: String, amount: Int): IdentityManagerPOA() {

    private val clients = mutableListOf<br.com.ajeferson.corba.Agenda>()
    private val servers = mutableListOf<br.com.ajeferson.corba.Agenda>()

    private lateinit var agendaClient: AgendaImpl // Interface between clients and this server
    private lateinit var agendaServer: AgendaImpl // Interface between other severs and this server

    private lateinit var rootPoa: POA
    private lateinit var namingContext: NamingContext

    private val disposables = CompositeDisposable()

    private val contacts = mutableListOf<Contact>()

    private val agendaId: String = "agenda$id"

    init {

        try {

            val args = arrayOf("[-ORBInitialHost", "$ip]", "agenda$id")
            val orb = ORB.init(args, null)

            val objPoa = orb.resolve_initial_references("RootPOA")
            val obj = orb.resolve_initial_references("NameService")

            rootPoa = POAHelper.narrow(objPoa)
            namingContext = NamingContextHelper.narrow(obj)

            // Bind Agendas
            agendaClient = AgendaImpl(args[2])
            agendaServer = AgendaImpl(args[2])

            // Interface for clients
            val clientObjRef = rootPoa.servant_to_reference(agendaClient)
            val clientComponents = arrayOf(NameComponent(agendaId, AgendaKind.AGENDA_CLIENT.description))

            // Interface for servers
            val serverObjRef = rootPoa.servant_to_reference(agendaServer)
            val serverComponents = arrayOf(NameComponent(agendaId, AgendaKind.AGENDA_SERVER.description))

            // Identity Manager
            val ref = rootPoa.servant_to_reference(this)
            val comp = arrayOf(NameComponent(args[2], IDENTITY_MANAGER_KIND))

            // Bind to name server
            namingContext.rebind(clientComponents, clientObjRef)
            namingContext.rebind(serverComponents, serverObjRef)
            namingContext.rebind(comp, ref)

            // Activation
            rootPoa.the_POAManager().activate()

            println("Server ${agendaId} is running at $ip")

            // Subscriptions
            observe(agendaClient, true)
            observe(agendaServer, false)

            connect(amount)

            orb.run()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observe(agenda: AgendaImpl, isClient: Boolean) {

        agenda
                .insertStream
                .subscribe {
                    didReceiveContact(isClient, it)
                }
                .disposedBy(disposables)

        agenda
                .removeStream
                .subscribe {
                    didRemoveContact(isClient, it)
                }
                .disposedBy(disposables)

        agenda
                .updateStream
                .subscribe {
                    didUpdateContact(isClient, it)
                }
                .disposedBy(disposables)

    }

    private fun connect(amount: Int) {

        var id = 0
        var sync = true

        while (id < amount) {

            id++

            if("agenda$id" == agendaId) {
                continue
            }

            try {

                // Get the Servers
                val name = arrayOf(NameComponent("agenda$id", IDENTITY_MANAGER_KIND))
                val objRef = namingContext.resolve(name)
                val identityManager = IdentityManagerHelper.narrow(objRef)

                val agendaRef = namingContext.resolve(arrayOf(NameComponent("agenda$id", AgendaKind.AGENDA_SERVER.description)))
                val server = AgendaHelper.narrow(agendaRef)

                // This triggers the sending of contacts by other agenda
                server.isAlive
                identityManager.identify(false, agendaId, sync)
                sync = false
                servers.add(server)

            } catch (e: Exception) {

            }

        }

    }


    /**
     * IdentityManager Implement
     * */

    override fun identify(isClient: Boolean, identity: String?, sync: Boolean) {

        if(identity == null) {
            return
        }

        val kind = if(isClient) AgendaKind.AGENDA_CLIENT.description else AgendaKind.AGENDA_SERVER.description
        val name = arrayOf(NameComponent(identity, kind))
        val ref = namingContext.resolve(name)

        val agenda = AgendaHelper.narrow(ref)

        if(isClient) {
            clients.add(agenda)
        } else {
            servers.add(agenda)
        }

        if(!sync) { return }

        // Send all my data to this new client
        contacts.forEach { contact ->
            try {
                agenda.insert(contact.name, contact.phoneNumber)
            } catch (e: Exception) {

            }
        }

    }

    private fun receivers(isClient: Boolean): List<Agenda> {
        val rec = clients.map { it }.toMutableList()
        if(isClient) {
            rec.addAll(servers.map { it })
        }
        return rec
    }



    /**
     * Contacts
     * */

    private fun didReceiveContact(isClient: Boolean, contact: Contact) {

        contacts.add(contact)

        val receivers = receivers(isClient)
        receivers.forEach { agenda ->
            try {
                agenda.insert(contact.name, contact.phoneNumber)
            } catch (e: Exception) {

            }
        }


    }

    private fun didRemoveContact(isClient: Boolean, name: String) {

        contacts.removeIf { it.name == name }

        val receivers = receivers(isClient)
        receivers.forEach { agenda ->
            try {
                agenda.remove(name)
            } catch (e: Exception) {

            }
        }

    }

    private fun didUpdateContact(isClient: Boolean, dto: UpdateContactDto) {

        val index = contacts.indexOfFirst { it.name == dto.oldName }

        if(index < 0) {
            return
        }

        contacts[index] = dto.update.copy()

        val receivers = receivers(isClient)
        receivers.forEach { agenda ->
            try {
                agenda.update(dto.oldName, dto.update.name, dto.update.phoneNumber)
            } catch (e: Exception) {

            }
        }

    }

    companion object {

        const val IDENTITY_MANAGER_KIND = "IdentityManager"

    }

}