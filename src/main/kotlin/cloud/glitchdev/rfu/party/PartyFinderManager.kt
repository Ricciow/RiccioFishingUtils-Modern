package cloud.glitchdev.rfu.party

import cloud.glitchdev.rfu.events.managers.party.PartyCreatedEventManager
import cloud.glitchdev.rfu.events.managers.party.PartyListChangedEventManager
import cloud.glitchdev.rfu.events.managers.party.PartyUpdatedEventManager
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.utils.dsl.isIgnored
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import java.util.concurrent.CopyOnWriteArrayList

object PartyFinderManager {
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
        PartyUpdatedEventManager.runTasks(party)
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
            PartyCreatedEventManager.runTasks(party)
        }
    }

    fun refreshParties() {
        PartyListChangedEventManager.runTasks(parties)
    }
}
