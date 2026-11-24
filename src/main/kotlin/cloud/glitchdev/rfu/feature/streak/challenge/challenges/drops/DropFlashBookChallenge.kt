package cloud.glitchdev.rfu.feature.streak.challenge.challenges.drops

import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.loot.registerRareDropEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object DropFlashBookChallenge : BaseChallenge() {
    override val id: String = "drop_flash_book"
    override val title: String = "Flash of Light"
    override val description: String = "Drop a Flash I Enchanted Book."
    override val weight: Int = 10

    override fun getTargetProgress(streakDays: Int): Int = 1

    override fun setupListeners(scope: EventScope) {
        scope.registerRareDropEvent { drop, _, _ ->
            if (drop == RareDrops.FLASH_BOOK) {
                addProgress(1)
            }
            true
        }
    }
}
