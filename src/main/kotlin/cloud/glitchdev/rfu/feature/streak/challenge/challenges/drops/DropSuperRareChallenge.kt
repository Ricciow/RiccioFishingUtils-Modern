package cloud.glitchdev.rfu.feature.streak.challenge.challenges.drops

import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.events.managers.DropEvents.registerRareDropEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeLevel
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object DropSuperRareChallenge : BaseChallenge() {
    override val id: String = "drop_super_rare"
    override val title: String = "Jackpot Drop!"
    override val description: String = "Drop a Radioactive Vial, Burnt Texts, Tiki Mask, Titanoboa Shed, Snake Eyes, Prince's Crown Jewel, Scuttler Shell, or Pyroclasm Book."
    override val weight: Int = 1

    private val SUPER_RARE_DROPS = setOf(
        RareDrops.RADIOACTIVE_VIAL,
        RareDrops.BURNT_TEXTS,
        RareDrops.TIKI_MASK,
        RareDrops.TITANOBOA_SHED,
        RareDrops.SNAKE_EYES,
        RareDrops.PRINCE_CROWN_JEWEL,
        RareDrops.SCUTTLER_SHELL,
        RareDrops.PYROCLASM_BOOK
    )

    override fun getTargetProgress(streakDays: Int): Int = 1

    override fun getLevel(streakDays: Int): ChallengeLevel = ChallengeLevel.ELITE

    override fun setupListeners() {
        activeListeners.add(registerRareDropEvent { drop, _ ->
            if (drop in SUPER_RARE_DROPS) {
                addProgress(1)
            }
            true
        })
    }
}
