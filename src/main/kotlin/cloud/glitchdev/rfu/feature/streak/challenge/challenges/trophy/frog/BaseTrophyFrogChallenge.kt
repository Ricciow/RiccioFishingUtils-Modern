package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.frog

import cloud.glitchdev.rfu.constants.fishing.TrophyFrog
import cloud.glitchdev.rfu.constants.skyblock.Rarity
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFrogCatchEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge

abstract class BaseTrophyFrogChallenge(
    override val id: String,
    val trophyFrog: TrophyFrog,
    override val title: String
) : BaseChallenge() {
    override val weight: Int
        get() = when (trophyFrog.rarity) {
            Rarity.COMMON -> 30
            Rarity.UNCOMMON -> 16
            Rarity.RARE -> 10
            Rarity.EPIC -> 6
            Rarity.LEGENDARY -> 8
            else -> 10
        }

    override fun getTargetProgress(streakDays: Int): Int {
        return when (trophyFrog) {
            TrophyFrog.PUDDLE_JUMPER -> when {
                streakDays < 7 -> 4
                streakDays < 14 -> 8
                else -> 12
            }
            else -> when (trophyFrog.rarity) {
                Rarity.COMMON -> when {
                    streakDays < 7 -> 15
                    streakDays < 14 -> 30
                    streakDays < 21 -> 50
                    else -> 75
                }
                Rarity.UNCOMMON -> when {
                    streakDays < 7 -> 8
                    streakDays < 14 -> 15
                    streakDays < 21 -> 25
                    else -> 35
                }
                Rarity.RARE -> when {
                    streakDays < 7 -> 2
                    streakDays < 14 -> 4
                    streakDays < 21 -> 6
                    else -> 8
                }
                Rarity.EPIC -> when {
                    streakDays < 7 -> 1
                    streakDays < 14 -> 2
                    streakDays < 21 -> 3
                    else -> 4
                }
                Rarity.LEGENDARY -> when {
                    streakDays < 15 -> 1
                    else -> 2
                }
                else -> 1
            }
        }
    }

    override fun getTitle(streakDays: Int): String = title

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        val name = trophyFrog.displayName
        return if (target == 1) {
            "Fish up 1 $name (any tier) today."
        } else {
            "Fish up $target $name (any tier) today."
        }
    }

    override fun setupListeners() {
        activeListeners.add(registerTrophyFrogCatchEvent { frog, _, amount ->
            if (frog == trophyFrog) {
                addProgress(amount)
            }
        })
    }
}
