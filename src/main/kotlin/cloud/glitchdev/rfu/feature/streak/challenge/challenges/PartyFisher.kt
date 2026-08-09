package cloud.glitchdev.rfu.feature.streak.challenge.challenges

import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerPartyJoinedEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object PartyFisher : BaseChallenge() {
    override val id: String = "party_fisher"
    override val title: String = "Party Fisher"
    override val description: String = "Create a party and have someone join you with rfupf or Join a party through rfupf"

    override fun getTargetProgress(streakDays: Int): Int {
        return 1
    }

    override fun setupListeners() {
        activeListeners.add(registerPartyJoinedEvent {
            addProgress(1)
        })
    }
}