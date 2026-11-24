package cloud.glitchdev.rfu.feature.streak.challenge.challenges.general

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerTickEvent
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object DailyAnglerChallenge : BaseChallenge() {
    override val id = "daily_angler"
    override val title = "Daily Angler"
    override val description = "Fish for the required duration."
    override val weight = 1000

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 7 -> 15
            streakDays < 14 -> 30
            streakDays < 21 -> 45
            else -> 60
        }
    }

    override fun getTitle(streakDays: Int): String {
        return when {
            streakDays < 7 -> "Novice Fisher"
            streakDays < 14 -> "Frequent Fisher"
            streakDays < 21 -> "Daily Fisher"
            else -> "Master Fisher"
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        return "Fish for $target minutes."
    }

    override fun setupListeners(scope: EventScope) {
        var lastCheckTimestamp = 0L
        var accumulatedMs = 0L

        scope.registerTickEvent(interval = 20) {
            if (FishingSession.isFishing && !FishingSession.isPaused) {
                val now = System.currentTimeMillis()
                if (lastCheckTimestamp > 0L) {
                    val delta = now - lastCheckTimestamp
                    if (delta in 1..10_000) {
                        accumulatedMs += delta
                        val minutes = (accumulatedMs / 60_000L).toInt()
                        if (minutes > 0) {
                            accumulatedMs %= 60_000L
                            addProgress(minutes)
                        }
                    }
                }
                lastCheckTimestamp = now
            } else {
                lastCheckTimestamp = 0L
            }
        }
    }
}
