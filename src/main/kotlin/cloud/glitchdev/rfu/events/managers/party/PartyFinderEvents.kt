package cloud.glitchdev.rfu.events.managers.party

import cloud.glitchdev.rfu.events.AbstractEventManager.Companion.DEFAULT_EVENT_PRIORITY
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.dsl.isIgnored
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import java.util.concurrent.CopyOnWriteArrayList

object PartyFinderEvents {
    private val _parties = CopyOnWriteArrayList<FishingParty>()
    val parties: List<FishingParty> get() = _parties
        .filter { !it.user.isIgnored() }
        .sortedBy { it.players.current >= it.players.max }

    fun handleSync(newParties: List<FishingParty>) {
        _parties.clear()
        _parties.addAll(newParties)
        refreshParties()
    }

    fun handleUpdate(party: FishingParty) {
        val index = _parties.indexOfFirst { it.user == party.user }
        if (index >= 0) {
            _parties[index] = party
        } else {
            _parties.add(0, party)
        }
        refreshParties()
        PartyUpdated.runTasks(party)
    }

    fun handleDelete(user: String) {
        _parties.removeIf { it.user == user }
        refreshParties()
    }

    fun handleCreated(party: FishingParty) {
        val existed = _parties.removeIf { it.user == party.user }
        if (existed) {
            PartyWebSocket.syncParties()
        }
        _parties.add(0, party)
        refreshParties()
        if (party.user.isUser()) {
            PartyCreated.runTasks(party)
        }
    }

    fun refreshParties() {
        PartyListChanged.runTasks(parties)
    }

    fun registerPartyCreatedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FishingParty) -> Unit): PartyCreated.PartyCreatedEvent {
        return PartyCreated.register(priority, callback)
    }

    fun registerPartyUpdatedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FishingParty) -> Unit): PartyUpdated.PartyUpdatedEvent {
        return PartyUpdated.register(priority, callback)
    }

    fun registerPartyJoinedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: () -> Unit): PartyJoined.PartyJoinedEvent {
        return PartyJoined.register(priority, callback)
    }

    fun registerPartyListChangedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (List<FishingParty>) -> Unit) = PartyListChanged.register(priority, callback)
    fun registerJoinRequestEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (String) -> Unit) = JoinRequest.register(priority, callback)
    fun registerInfoMessageEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (String, Boolean) -> Unit) = InfoMessage.register(priority, callback)
    fun registerMyPartyChangedEvent(priority: Int = DEFAULT_EVENT_PRIORITY, callback: (FishingParty?) -> Unit) = MyPartyChanged.register(priority, callback)

    fun runPartyJoinedTasks() {
        PartyJoined.runTasks()
    }
}
