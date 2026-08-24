package cloud.glitchdev.rfu.feature.streak.challenge.challenges.general

import cloud.glitchdev.rfu.events.managers.PartyFinderEvents.registerPartyJoinedEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object PartyFisher : BaseChallenge() {
    override val id: String = "party_fisher"
    override val title: String = "Party Fisher"
    override val description: String = "Create a party and have someone join you with rfu party finder or Join a party through rfu party finder"
    override val weight: Int = 35

    override fun getTargetProgress(streakDays: Int): Int {
        return 1
    }

    override fun setupListeners() {
        activeListeners.add(registerPartyJoinedEvent {
            addProgress(1)
        })
    }
}
