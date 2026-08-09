package cloud.glitchdev.rfu.feature.streak.challenge.challenges.drops

import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.events.managers.DropEvents.registerRareDropEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeLevel
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object DropFlashBookChallenge : BaseChallenge() {
    override val id: String = "drop_flash_book"
    override val title: String = "Flash of Light"
    override val description: String = "Drop a Flash I Enchanted Book."
    override val weight: Int = 10

    override fun getTargetProgress(streakDays: Int): Int = 1

    override fun getLevel(streakDays: Int): ChallengeLevel = ChallengeLevel.INTERMEDIATE

    override fun setupListeners() {
        activeListeners.add(registerRareDropEvent { drop, _ ->
            if (drop == RareDrops.FLASH_BOOK) {
                addProgress(1)
            }
            true
        })
    }
}
