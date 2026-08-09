package cloud.glitchdev.rfu.feature.streak.challenge.challenges.trophy.fish

import cloud.glitchdev.rfu.constants.fishing.TrophyFish
import cloud.glitchdev.rfu.constants.skyblock.Rarity
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFishCatchEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeLevel

abstract class BaseTrophyFishChallenge(
    override val id: String,
    val trophyFish: TrophyFish,
    override val title: String
) : BaseChallenge() {
    override val weight: Int
        get() = when (trophyFish.rarity) {
            Rarity.COMMON -> 14
            Rarity.UNCOMMON -> 11
            Rarity.RARE -> 5
            Rarity.EPIC -> 4
            Rarity.LEGENDARY -> 5
            else -> 10
        }

    override fun getTargetProgress(streakDays: Int): Int {
        return when (trophyFish.rarity) {
            Rarity.COMMON -> when {
                streakDays < 7 -> 15
                streakDays < 14 -> 30
                streakDays < 21 -> 50
                else -> 75
            }
            Rarity.UNCOMMON -> when {
                streakDays < 7 -> 10
                streakDays < 14 -> 20
                streakDays < 21 -> 30
                else -> 40
            }
            Rarity.RARE -> when {
                streakDays < 7 -> 3
                streakDays < 14 -> 5
                streakDays < 21 -> 8
                else -> 10
            }
            Rarity.EPIC -> when {
                streakDays < 7 -> 1
                streakDays < 14 -> 2
                streakDays < 21 -> 3
                else -> 4
            }
            Rarity.LEGENDARY -> when {
                streakDays < 15 -> 1
                streakDays < 30 -> 2
                else -> 3
            }
            else -> 1
        }
    }

    override fun getTitle(streakDays: Int): String = title

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        val name = trophyFish.displayName
        return if (target == 1) {
            "Fish up 1 $name (any tier) today."
        } else {
            "Fish up $target $name (any tier) today."
        }
    }

    override fun getLevel(streakDays: Int): ChallengeLevel {
        return when (trophyFish.rarity) {
            Rarity.COMMON -> ChallengeLevel.BASIC
            Rarity.UNCOMMON -> ChallengeLevel.INTERMEDIATE
            Rarity.RARE -> ChallengeLevel.ADVANCED
            else -> ChallengeLevel.ELITE
        }
    }

    override fun setupListeners() {
        activeListeners.add(registerTrophyFishCatchEvent { fish, _, amount ->
            if (fish == trophyFish) {
                addProgress(amount)
            }
        })
    }
}
