package br.com.ajeferson.client.viewModel

import br.com.ajeferson.agenda.AgendaImpl
import br.com.ajeferson.client.protocol.TableDataSource
import br.com.ajeferson.client.protocol.TableDelegate
import br.com.ajeferson.corba.Agenda
import br.com.ajeferson.corba.AgendaHelper
import br.com.ajeferson.corba.IdentityManager
import br.com.ajeferson.corba.IdentityManagerHelper
import br.com.ajeferson.entity.Contact
import br.com.ajeferson.entity.UpdateContactDto
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
import java.util.*

class ClientViewModel(
        private val ip: String,
        private val amount: Int,
        contactsStream: Observable<Contact>,
        removeStream: Observable<Int>,
        connectStream: Observable<Unit>,
        searchStream: Observable<String>): TableDataSource, TableDelegate {

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
            statusStream.onNext(value)
        }

    var error: String? = null
        set(value) {
            field = value
            if(value != null && value.isNotEmpty()) {
                errorStream.onNext(value)
            }
        }

    val statusStream: PublishSubject<Status> = PublishSubject.create()
    val agendaStream: PublishSubject<String> = PublishSubject.create()
    val reloadStream: PublishSubject<Unit> = PublishSubject.create()
    val errorStream: PublishSubject<String> = PublishSubject.create()
    val searchResultsStream: PublishSubject<List<Contact>> = PublishSubject.create()

    private val disposables = CompositeDisposable()


    init {

        contactsStream
                .subscribe {
                    if(contacts.firstOrNull { elem -> elem.name == it.name } != null) {
                        error = "Duplicated contact!"
                    } else {
                        try {
                            agendaServer?.insert(it.name, it.phoneNumber)
                        } catch (e: Exception) {
                            clear()
                        }
                    }
                }
                .disposedBy(disposables)

        removeStream
                .subscribe {
                    val contact = contacts[it]
                    try {
                        agendaServer?.remove(contact.name)
                    } catch (e: Exception) {
                        clear()
                    }
                }

        connectStream
                .subscribe {
                    connect()
                }
                .disposedBy(disposables)

        searchStream
                .subscribe {
                    search(it)
                }
                .disposedBy(disposables)

    }

    fun init() {
        status = Status.DISCONNECTED
        initCorba()
        connect()
    }

    private fun initCorba() {

        val args = arrayOf("[-ORBInitialHost", "$ip]")
        val orb = ORB.init(args, null)

        // Initial references
        val objPoa = orb.resolve_initial_references("RootPOA")
        val obj = orb.resolve_initial_references("NameService")

        namingContext = NamingContextHelper.narrow(obj)

        val rootPoa = POAHelper.narrow(objPoa)

        // Subscribe Client's agendaServer
        agendaClient = AgendaImpl("client0")
        val objRef = rootPoa.servant_to_reference(agendaClient)
        val components = arrayOf(NameComponent(agendaClient.id, AgendaKind.AGENDA_CLIENT.description))
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

        agendaClient
                .updateStream
                .subscribe {
                    didUpdateContact(it)
                }
                .disposedBy(disposables)

    }

    private fun connect() {

        // Can not connect twice
        if(status.isConnected) {
            return
        }

        status = Status.CONNECTING

        val ids = (1..amount).map { it }.toMutableList()
        var id = -1

        while (ids.isNotEmpty() && agendaServer == null) {

            val index = Random().nextInt(ids.size)
            id = ids[index]

            try {

                // Get the AgendaServer
                val name = arrayOf(NameComponent(agendaId(id), AgendaKind.AGENDA_CLIENT.description))
                val objRef = namingContext.resolve(name)
                agendaServer = AgendaHelper.narrow(objRef)
                agendaServer?.isAlive


                // Get the ClientServer
                val cName = arrayOf(NameComponent(agendaId(id), Server.IDENTITY_MANAGER_KIND))
                val cRef = namingContext.resolve(cName)
                identityManager = IdentityManagerHelper.narrow(cRef)
                identityManager?.identify(true, "client0", true) // TODO Refactor

            } catch (e: Exception) {
                agendaServer = null
                ids.removeAt(index)
            }

        }

        status = if(agendaServer != null) {
            agendaStream.onNext("Connected to ${agendaId(id)}")
            Status.CONNECTED
        } else {
            Status.DISCONNECTED
        }

    }

    private fun clear() {
        agendaServer = null
        contacts.clear()
        reloadStream.onNext(Unit)
        status = Status.DISCONNECTED
        error = "Dead connection"
    }

    private fun updateContact(index: Int, newContact: Contact) {
        try {
            val contact = contacts[index]
            agendaServer?.update(contact.name, newContact.name, newContact.phoneNumber)
        } catch (e: Exception) {
            clear()
        }
    }

    private fun search(query: String) {
        val term = query.trim().toLowerCase()
        val results = contacts.filter { it.name.toLowerCase().contains(term) ||
                it.phoneNumber.toLowerCase().contains(term) }
        val copy = results.map { it.copy() }
        searchResultsStream.onNext(copy)
    }



    /**
     * Called remotely from server
     * */

    private fun didReceiveContact(contact: Contact) {
        contacts.add(contact)
        contacts.sortBy { it.name }
        reloadStream.onNext(Unit)
    }

    private fun didRemoveContact(name: String) {
        contacts.removeIf { it.name == name }
        reloadStream.onNext(Unit)
    }

    private fun didUpdateContact(dto: UpdateContactDto) {
        val index = contacts.indexOfFirst { it.name == dto.oldName }
        if(index < 0) {
            return
        }
        contacts[index] = dto.update.copy()
        contacts.sortBy { it.name }
        reloadStream.onNext(Unit)
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



    /**
     * Table Delegate
     * */

    override fun didChangeName(index: Int, name: String) {
        val newContact = contacts[index].copy(name = name)
        updateContact(index, newContact)
    }

    override fun didChangePhoneNumber(index: Int, phoneNumber: String) {
        val newContact = contacts[index].copy(phoneNumber = phoneNumber)
        updateContact(index, newContact)
    }

    enum class Status(val description: String) {

        DISCONNECTED("Disconnected"),
        CONNECTING("Connecting"),
        CONNECTED("Connected");

        val isConnected: Boolean get() = this == CONNECTED
        val isDisconnected: Boolean get() = this == DISCONNECTED

    }

}