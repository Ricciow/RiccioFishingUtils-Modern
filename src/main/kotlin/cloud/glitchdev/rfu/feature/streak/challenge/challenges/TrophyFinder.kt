package cloud.glitchdev.rfu.feature.streak.challenge.challenges

import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFishCatchEvent
import cloud.glitchdev.rfu.events.managers.TrophyCatchEvents.registerTrophyFrogCatchEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeLevel
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object TrophyFinder : BaseChallenge() {
    override val id: String = "trophy_finder"
    override val title: String = "Trophy Finder"
    override val description: String = "Fish up trophy fishes/frogs"

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 7 -> 10
            streakDays < 14 -> 20
            streakDays < 21 -> 35
            else -> 50
        }
    }

    override fun getTitle(streakDays: Int): String {
        return when {
            streakDays < 7 -> "Trophy Finder"
            streakDays < 14 -> "Trophy Hunter"
            streakDays < 21 -> "Trophy Expert"
            else -> "Trophy Legend"
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return if (target == 1) {
            "Fish up a trophy fish/frog"
        } else {
            "Fish up $target trophy fishes/frogs"
        }
    }

    override fun getLevel(streakDays: Int): ChallengeLevel {
        return when {
            streakDays < 7 -> ChallengeLevel.BASIC
            streakDays < 14 -> ChallengeLevel.INTERMEDIATE
            streakDays < 21 -> ChallengeLevel.ADVANCED
            else -> ChallengeLevel.ELITE
        }
    }

    override fun setupListeners() {
        activeListeners.add(registerTrophyFishCatchEvent { _, _, amount ->
            addProgress(amount)
        })
        activeListeners.add(registerTrophyFrogCatchEvent { _, _, amount ->
            addProgress(amount)
        })
    }
}