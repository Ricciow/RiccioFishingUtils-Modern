package cloud.glitchdev.rfu.feature.streak.challenge.challenges.general

import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object CatchAnySeaCreatureChallenge : BaseChallenge() {
    override val id: String = "catch_any_sea_creature"
    override val title: String = "Sea Creature Hunter"
    override val description: String = "Catch sea creatures."
    override val weight: Int = 50

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 14 -> 50
            streakDays < 28 -> 100
            else -> 150
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return "Catch $target sea creatures."
    }

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { _, doubleHook, _, _, _ ->
            addProgress(if (doubleHook) 2 else 1)
        })
    }
}
