package cloud.glitchdev.rfu.feature.streak.challenge.challenges.general

import cloud.glitchdev.rfu.constants.skyblock.SkillType
import cloud.glitchdev.rfu.events.managers.SkillEvents.registerSkillXpUpdateEvent
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.RFUChallenge
import cloud.glitchdev.rfu.utils.SkillTracker
import java.text.NumberFormat
import java.util.Locale

@RFUChallenge
object GainFishingExpChallenge : BaseChallenge() {
    override val id: String = "gain_fishing_exp"
    override val title: String = "Fishing Scholar"
    override val description: String = "Gain Fishing EXP."
    override val weight: Int = 50

    override fun getTargetProgress(streakDays: Int): Int {
        return when {
            streakDays < 7 -> 500_000
            streakDays < 14 -> 1_000_000
            streakDays < 21 -> 1_500_000
            else -> 2_000_000
        }
    }

    override fun getTitle(streakDays: Int): String {
        return when {
            streakDays < 7 -> "Novice Scholar"
            streakDays < 14 -> "Adept Scholar"
            streakDays < 21 -> "Expert Scholar"
            else -> "Master Scholar"
        }
    }

    override fun getDescription(streakDays: Int): String {
        val target = getTargetProgress(streakDays)
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(target)
        return "Gain $formatted Fishing EXP."
    }

    private var lastKnownXp: Long = 0L

    override fun setupListeners() {
        lastKnownXp = SkillTracker.getSkillXp(SkillType.FISHING)

        activeListeners.add(registerSkillXpUpdateEvent(SkillType.FISHING) { _, xp ->
            if (lastKnownXp in 1..<xp) {
                val diff = xp - lastKnownXp
                if (diff in 1..2_000_000L) {
                    addProgress(diff.toInt())
                }
            }
            lastKnownXp = xp
        })
    }
}
