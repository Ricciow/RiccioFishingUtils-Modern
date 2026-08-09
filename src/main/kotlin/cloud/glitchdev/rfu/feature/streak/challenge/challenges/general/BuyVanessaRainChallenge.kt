package cloud.glitchdev.rfu.feature.streak.challenge.challenges.general

import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeLevel
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@RFUChallenge
object BuyVanessaRainChallenge : BaseChallenge() {
    override val id: String = "buy_vanessa_rain"
    override val title: String = "Rainmaker"
    override val description: String = "Buy rain minutes at Vanessa."
    override val weight: Int = 25

    private val RAIN_REGEX = """You added (?:a|(\d+)) minutes? of rain""".toExactRegex()

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 14 -> 15
            streakDays < 28 -> 25
            else -> 40
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return "Buy $target minutes of rain at Vanessa."
    }

    override fun getLevel(streakDays: Int): ChallengeLevel {
        return when {
            streakDays < 14 -> ChallengeLevel.BASIC
            streakDays < 28 -> ChallengeLevel.INTERMEDIATE
            else -> ChallengeLevel.ADVANCED
        }
    }

    override fun setupListeners() {
        activeListeners.add(registerGameEvent(RAIN_REGEX, isOverlay = false) { _, _, matches ->
            val minutes = matches?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
            addProgress(minutes)
        })
    }
}
