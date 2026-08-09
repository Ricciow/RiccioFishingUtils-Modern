package cloud.glitchdev.rfu.feature.streak.challenge.challenges

import cloud.glitchdev.rfu.config.categories.DailyStreakSettings
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.fishing.FishingSession
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeLevel
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge

@RFUChallenge
object DailyAnglerChallenge : BaseChallenge() {
    override val id = "daily_angler"
    override val title = "Daily Angler"
    override val description = "Fish for the required duration today."
    override val level = ChallengeLevel.BASIC
    override val isMandatoryBase = true

    private var secondCounter = 0

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 7 -> 15
            streakDays < 14 -> 30
            streakDays < 21 -> 45
            else -> 60
        }
    }

    override fun setupListeners() {
        unregisterListeners()
        secondCounter = 0
        activeListeners.add(registerTickEvent(interval = 20) {
            if (!DailyStreakSettings.dailyStreakEnabled) return@registerTickEvent

            if (FishingSession.isFishing && !FishingSession.isPaused) {
                secondCounter++
                if (secondCounter >= 60) {
                    secondCounter = 0
                    addProgress(1)
                }
            }
        })
    }
}
