package cloud.glitchdev.rfu.feature.streak.challenge.challenges.general

import cloud.glitchdev.rfu.events.managers.CocoonEvents.registerCocoonEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object CocoonSeaCreaturesChallenge : BaseChallenge() {
    override val id: String = "cocoon_sea_creatures"
    override val title: String = "Cocoon Master"
    override val description: String = "Cocoon sea creatures today."
    override val weight: Int = 50

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 14 -> 10
            streakDays < 28 -> 25
            else -> 40
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return "Cocoon $target sea creatures today."
    }

    override fun setupListeners() {
        activeListeners.add(registerCocoonEvent { _ ->
            addProgress(1)
        })
    }
}
