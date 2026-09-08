package cloud.glitchdev.rfu.feature.streak.challenge.challenges.general

import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object AnyB2BSeaCreatureChallenge : BaseChallenge() {
    override val id: String = "b2b_sea_creatures"
    override val title: String = "Back for more"
    override val description: String = "Catch the any sea creature back to back."
    override val weight: Int = 25

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 14 -> 15
            else -> 30
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return "Catch the any sea creature back to back $target times."
    }

    private var lastSc: String? = null

    override fun setupListeners() {
        lastSc = null
        activeListeners.add(registerSeaCreatureCatchEvent { sc, _, _, _, _ ->
            if (lastSc == sc.scName) {
                addProgress(1)
                lastSc = null
            } else {
                lastSc = sc.scName
            }
        })
    }
}
