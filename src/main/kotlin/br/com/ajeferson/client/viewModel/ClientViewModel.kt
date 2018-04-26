package br.com.ajeferson.client.viewModel

import br.com.ajeferson.client.AgendaImpl
import br.com.ajeferson.client.protocol.TableDataSource
import br.com.ajeferson.corba.Agenda
import br.com.ajeferson.corba.AgendaHelper
import br.com.ajeferson.corba.IdentityManager
import br.com.ajeferson.corba.IdentityManagerHelper
import br.com.ajeferson.entity.Contact
import br.com.ajeferson.enumeration.AgendaKind
import br.com.ajeferson.extension.disposedBy
import br.com.ajeferson.server.Server
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.omg.CORBA.ORB
import org.omg.CosNaming.NameComponent
import org.omg.CosNaming.NamingContext
import org.omg.CosNaming.NamingContextHelper
import org.omg.PortableServer.POAHelper

class ClientViewModel(contactsStream: Observable<Contact>, removeStream: Observable<Int>): TableDataSource {

    private lateinit var namingContext: NamingContext

    // Agendas
    var agendaServer: Agenda? = null
    private lateinit var agendaClient: AgendaImpl

    private var identityManager: IdentityManager? = null

    private val contacts = mutableListOf<Contact>()
    private val columns = listOf("Name", "Phone Number")


    var status = Status.DISCONNECTED
        set(value) {
            field = value
            if(!value.isConnected) {
                statusStream.onNext(value)
            }
        }

    val statusStream: PublishSubject<Status> = PublishSubject.create()
    val agendaStream: PublishSubject<String> = PublishSubject.create()
    val reloadStream: PublishSubject<Boolean> = PublishSubject.create()

    private val disposables = CompositeDisposable()


    init {

        contactsStream
                .subscribe {
                    agendaServer?.insert(it.name, it.phoneNumber)
                }
                .disposedBy(disposables)

        removeStream
                .subscribe {
                    val contact = contacts[it]
                    agendaServer?.remove(contact.name)
                }

    }

    fun init() {
        status = Status.DISCONNECTED
        initCorba()
        connect()
    }

    private fun initCorba() {

        val orb = ORB.init(arrayOf(), null)

        // Initial references
        val objPoa = orb.resolve_initial_references("RootPOA")
        val obj = orb.resolve_initial_references("NameService")

        namingContext = NamingContextHelper.narrow(obj)

        val rootPoa = POAHelper.narrow(objPoa)

        // Subscribe Client's agendaServer
        agendaClient = AgendaImpl("client0")
        val objRef = rootPoa.servant_to_reference(agendaClient)
        val components = arrayOf(NameComponent(agendaClient.id, AgendaKind.AGENDA.description))
        namingContext.rebind(components, objRef)

        rootPoa.the_POAManager().activate()

        // Subscribe to the new contact stream
        agendaClient
                .insertStream
                .subscribe {
                    didReceiveContact(it)
                }
                .disposedBy(disposables)

        agendaClient
                .removeStream
                .subscribe {
                    didRemoveContact(it)
                }
                .disposedBy(disposables)

    }

    private fun connect() {

        // Can not connect twice
        if(status.isConnected) {
            return
        }

        status = Status.CONNECTING

        var id = 0

        while (id < NUMBER_OF_AGENDAS && agendaServer == null) {

            id++

            try {

                // Get the AgendaServer
                val name = arrayOf(NameComponent(agendaId(id), AgendaKind.AGENDA.description))
                val objRef = namingContext.resolve(name)
                agendaServer = AgendaHelper.narrow(objRef)
                agendaServer?.isAlive


                // Get the ClientServer
                val cName = arrayOf(NameComponent(agendaId(id), Server.IDENTITY_MANAGER_KIND))
                val cRef = namingContext.resolve(cName)
                identityManager = IdentityManagerHelper.narrow(cRef)
                identityManager?.identify("client0") // TODO Refactor

            } catch (e: Exception) {
                agendaServer = null
            }

        }

        status = if(agendaServer != null) {
            agendaStream.onNext("Connected to ${agendaId(id)}")

            Status.CONNECTED
        } else {
            Status.DISCONNECTED
        }

    }


    /**
     * Called remotely from server
     * */

    private fun didReceiveContact(contact: Contact) {
        contacts.add(contact)
        reloadStream.onNext(true)
    }

    private fun didRemoveContact(name: String) {
        contacts.removeIf { it.name == name }
        reloadStream.onNext(true)
    }



    /**
     * Helper
     * */

    private fun agendaId(number: Int) = "agenda$number"

    /**
     * Table Data Source
     * */

    override fun numberOfRows() = contacts.size

    override fun numberOfColumns() = columns.size

    override fun columnNameAt(index: Int) = columns[index]

    override fun valueAt(row: Int, column: Int) = when(column) {
        0 -> contacts[row].name
        else -> contacts[row].phoneNumber
    }

    enum class Status(val description: String) {

        DISCONNECTED("Disconnected"),
        CONNECTING("Connecting"),
        CONNECTED("Connected");

        val isConnected: Boolean get() = this == CONNECTED

    }

    companion object {
        private const val NUMBER_OF_AGENDAS = 3
    }

}