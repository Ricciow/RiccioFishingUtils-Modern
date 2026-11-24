package cloud.glitchdev.rfu.feature.streak.challenge.challenges.drops

import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.loot.registerRareDropEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object DropLuckyCloverCoreChallenge : BaseChallenge() {
    override val id: String = "drop_lucky_clover_core"
    override val title: String = "Four Leaf Clover"
    override val description: String = "Drop a Lucky Clover Core."
    override val weight: Int = 10

    override fun getTargetProgress(streakDays: Int): Int = 1

    override fun setupListeners(scope: EventScope) {
        scope.registerRareDropEvent { drop, _, _ ->
            if (drop == RareDrops.LUCKY_CLOVER_CORE) {
                addProgress(1)
            }
            true
        }
    }
}
