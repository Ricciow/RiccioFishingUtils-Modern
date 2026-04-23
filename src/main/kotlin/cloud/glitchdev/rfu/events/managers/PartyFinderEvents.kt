package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.model.party.FishingParty
import java.util.concurrent.CopyOnWriteArrayList

object PartyFinderEvents {
    private val _parties = CopyOnWriteArrayList<FishingParty>()
    val parties: List<FishingParty> get() = _parties

    object PartyCreated : AbstractEventManager<(FishingParty) -> Unit, PartyCreated.PartyCreatedEvent>() {
        override val runTasks: (FishingParty) -> Unit = { party ->
            safeExecution {
                tasks.forEach { task ->
                    task.callback(party)
                }
            }
        }

        fun register(priority: Int = 20, callback: (FishingParty) -> Unit): PartyCreatedEvent {
            return PartyCreatedEvent(priority, callback).register()
        }

        class PartyCreatedEvent(
            priority: Int = 20,
            callback: (FishingParty) -> Unit
        ) : ManagedTask<(FishingParty) -> Unit, PartyCreatedEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object PartyUpdated : AbstractEventManager<(FishingParty) -> Unit, PartyUpdated.PartyUpdatedEvent>() {
        override val runTasks: (FishingParty) -> Unit = { party ->
            safeExecution {
                tasks.forEach { task ->
                    task.callback(party)
                }
            }
        }

        fun register(priority: Int = 20, callback: (FishingParty) -> Unit): PartyUpdatedEvent {
            return PartyUpdatedEvent(priority, callback).register()
        }

        class PartyUpdatedEvent(
            priority: Int = 20,
            callback: (FishingParty) -> Unit
        ) : ManagedTask<(FishingParty) -> Unit, PartyUpdatedEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object PartyJoined : AbstractEventManager<() -> Unit, PartyJoined.PartyJoinedEvent>() {
        override val runTasks: () -> Unit = {
            safeExecution {
                tasks.forEach { task ->
                    task.callback()
                }
            }
        }

        fun register(priority: Int = 20, callback: () -> Unit): PartyJoinedEvent {
            return PartyJoinedEvent(priority, callback).register()
        }

        class PartyJoinedEvent(
            priority: Int = 20,
            callback: () -> Unit
        ) : ManagedTask<() -> Unit, PartyJoinedEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object PartyListChanged : AbstractEventManager<(List<FishingParty>) -> Unit, PartyListChanged.PartyListChangedEvent>() {
        override val runTasks: (List<FishingParty>) -> Unit = { parties ->
            safeExecution {
                tasks.forEach { it.callback(parties) }
            }
        }

        fun register(priority: Int = 20, callback: (List<FishingParty>) -> Unit): PartyListChangedEvent {
            return PartyListChangedEvent(priority, callback).register()
        }

        class PartyListChangedEvent(priority: Int, callback: (List<FishingParty>) -> Unit) : ManagedTask<(List<FishingParty>) -> Unit, PartyListChangedEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object JoinRequest : AbstractEventManager<(String) -> Unit, JoinRequest.JoinRequestEvent>() {
        override val runTasks: (String) -> Unit = { applicant ->
            safeExecution {
                tasks.forEach { it.callback(applicant) }
            }
        }

        fun register(priority: Int = 20, callback: (String) -> Unit): JoinRequestEvent {
            return JoinRequestEvent(priority, callback).register()
        }

        class JoinRequestEvent(priority: Int, callback: (String) -> Unit) : ManagedTask<(String) -> Unit, JoinRequestEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    object MyPartyChanged : AbstractEventManager<(FishingParty?) -> Unit, MyPartyChanged.MyPartyChangedEvent>() {
        override val runTasks: (FishingParty?) -> Unit = { party ->
            safeExecution {
                tasks.forEach { it.callback(party) }
            }
        }

        fun register(priority: Int = 20, callback: (FishingParty?) -> Unit): MyPartyChangedEvent {
            return MyPartyChangedEvent(priority, callback).register()
        }

        class MyPartyChangedEvent(priority: Int, callback: (FishingParty?) -> Unit) : ManagedTask<(FishingParty?) -> Unit, MyPartyChangedEvent>(priority, callback) {
            override fun register() = submitTask(this)
            override fun unregister() = removeTask(this)
        }
    }

    fun handleSync(newParties: List<FishingParty>) {
        _parties.clear()
        _parties.addAll(newParties)
        PartyListChanged.runTasks(_parties.toList())
    }

    fun handleUpdate(party: FishingParty) {
        _parties.removeIf { it.user == party.user }
        _parties.add(party)
        PartyListChanged.runTasks(_parties.toList())
        PartyUpdated.runTasks(party)
    }

    fun handleDelete(user: String) {
        _parties.removeIf { it.user == user }
        PartyListChanged.runTasks(_parties.toList())
    }

    fun handleCreated(party: FishingParty) {
        _parties.removeIf { it.user == party.user }
        _parties.add(party)
        PartyListChanged.runTasks(_parties.toList())
        PartyCreated.runTasks(party)
    }

    fun registerPartyCreatedEvent(priority: Int = 20, callback: (FishingParty) -> Unit): PartyCreated.PartyCreatedEvent {
        return PartyCreated.register(priority, callback)
    }

    fun registerPartyUpdatedEvent(priority: Int = 20, callback: (FishingParty) -> Unit): PartyUpdated.PartyUpdatedEvent {
        return PartyUpdated.register(priority, callback)
    }

    fun registerPartyJoinedEvent(priority: Int = 20, callback: () -> Unit): PartyJoined.PartyJoinedEvent {
        return PartyJoined.register(priority, callback)
    }

    fun registerPartyListChangedEvent(priority: Int = 20, callback: (List<FishingParty>) -> Unit) = PartyListChanged.register(priority, callback)
    fun registerJoinRequestEvent(priority: Int = 20, callback: (String) -> Unit) = JoinRequest.register(priority, callback)
    fun registerMyPartyChangedEvent(priority: Int = 20, callback: (FishingParty?) -> Unit) = MyPartyChanged.register(priority, callback)

    fun runPartyJoinedTasks() {
        PartyJoined.runTasks()
    }
}
