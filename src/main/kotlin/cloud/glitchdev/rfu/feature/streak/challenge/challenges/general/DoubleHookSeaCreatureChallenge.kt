package cloud.glitchdev.rfu.feature.streak.challenge.challenges.general

import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object DoubleHookSeaCreatureChallenge : BaseChallenge() {
    override val id: String = "double_hook_sea_creatures"
    override val title: String = "Double Trouble"
    override val description: String = "Double hook sea creatures."
    override val weight: Int = 25

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 14 -> 15
            streakDays < 30 -> 20
            else -> 30
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return "Double hook $target sea creatures."
    }

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { _, isDoubleHook, _, _, _ ->
            if (isDoubleHook) {
                addProgress(1)
            }
        })
    }
}
