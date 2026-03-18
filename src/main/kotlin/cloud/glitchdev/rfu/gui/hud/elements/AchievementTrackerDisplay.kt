package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementManager
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.RED
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.data.achievements.AchievementHandler
import cloud.glitchdev.rfu.events.managers.AchievementStageUnlockedEvents.registerAchievementStageUnlockedEvent
import cloud.glitchdev.rfu.events.managers.AchievementUnlockedEvents.registerAchievementUnlockedEvent
import cloud.glitchdev.rfu.events.managers.AchievementUpdatedEvents.registerAchievementUpdatedEvent
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement

import cloud.glitchdev.rfu.utils.dsl.compact

@HudElement
object AchievementTrackerDisplay : AbstractTextHudElement("achievementTrackerDisplay") {

    override val enabled: Boolean
        get() = OtherSettings.achievementTrackerDisplay && (super.enabled || AchievementHandler.getTrackedAchievements().isNotEmpty())

    override fun onInitialize() {
        super.onInitialize()
        registerAchievementUpdatedEvent { _ ->
            updateState()
        }
        registerAchievementUnlockedEvent { _ ->
            updateState()
        }
        registerAchievementStageUnlockedEvent { _ ->
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        val trackedIds = AchievementHandler.getTrackedAchievements()
        if (trackedIds.isEmpty()) {
            if (isEditing) {
                text.setText("Achievement Tracker")
            } else {
                text.setText("")
            }
            return
        }

        val lines = mutableListOf<String>()
        trackedIds.forEach { id ->
            val achievement = AchievementManager.getAchievement(id)
            if (achievement != null) {
                val name = if (achievement is IStageAchievement && !achievement.isCompleted) {
                    achievement.getStageName(achievement.currentStage) ?: achievement.name
                } else {
                    achievement.name
                }

                val difficulty = if (achievement is IStageAchievement && !achievement.isCompleted) {
                    achievement.getStageDifficulty(achievement.currentStage) ?: achievement.difficulty
                } else {
                    achievement.difficulty
                }

                val color = when (difficulty) {
                    AchievementDifficulty.EASY -> LIGHT_GREEN
                    AchievementDifficulty.MEDIUM -> YELLOW
                    AchievementDifficulty.HARD -> LIGHT_RED
                    AchievementDifficulty.VERY_HARD -> RED
                    AchievementDifficulty.IMPOSSIBLE -> RED // Closest to DARK_RED if not available
                }

                val current = achievement.currentProgress
                val target = achievement.targetProgress
                val percentage = if (target > 0L) (current.toFloat() / target.toFloat() * 100).toInt() else 0

                val progressText = if (achievement.isCompleted) {
                    "Completed!"
                } else {
                    "${current.compact()}/${target.compact()} $GRAY($percentage%)"
                }

                lines.add("$color$name: $YELLOW$progressText")
            }
        }

        text.setText(if (lines.isEmpty()) (if (isEditing) "Achievement Tracker" else "") else lines.joinToString("\n"))
    }
}
