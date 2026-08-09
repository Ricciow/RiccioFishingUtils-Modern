package cloud.glitchdev.rfu.feature.streak.challenge.challenges

import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeLevel
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object PreciousTrophy : BaseChallenge() {
    override val id: String = "precious_trophy"
    override val title: String = "Precious Trophy"
    override val description: String = "Fish up a gold or diamond trophy fish/frog"

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 15 -> 1
            streakDays < 30 -> 2
            else -> 3
        }
    }

    override fun getTitle(streakDays: Int): String {
        return when {
            streakDays < 15 -> "Precious Trophy"
            streakDays < 30 -> "Rare Collector"
            else -> "Special Collection"
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return if (target == 1) {
            "Fish up a gold or diamond trophy fish/frog"
        } else {
            "Fish up $target gold or diamond trophy fishes/frogs"
        }
    }

    override fun getLevel(streakDays: Int): ChallengeLevel {
        return when {
            streakDays < 15 -> ChallengeLevel.INTERMEDIATE
            streakDays < 30 -> ChallengeLevel.ADVANCED
            else -> ChallengeLevel.ELITE
        }
    }
}