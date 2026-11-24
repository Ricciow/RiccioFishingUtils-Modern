package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementManager
import cloud.glitchdev.rfu.achievement.AchievementProvider
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.data.achievements.AchievementHandler
import cloud.glitchdev.rfu.events.managers.registerAchievementStageUnlockedEvent
import cloud.glitchdev.rfu.events.managers.registerAchievementUnlockedEvent
import cloud.glitchdev.rfu.events.managers.registerAchievementUpdatedEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.compact
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.toConstraint

@HudElement
object AchievementTrackerDisplay : AbstractHudElement("achievementTrackerDisplay") {
    override val relatedSetting = "Achievement Tracker Display"
    override val requirement: Boolean
        get() = OtherSettings.achievementTrackerDisplay
    override val isElementActive: Boolean
        get() = AchievementHandler.getTrackedAchievements().any { getVisibleAchievement(it) != null }

    private fun getVisibleAchievement(id: String) =
        AchievementManager.getAchievement(id)?.takeIf(AchievementProvider::isVisible)

    private val container = UIContainer().constrain {
        width = ChildBasedMaxSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf this

    private var achievementLines: List<UIText> = emptyList()

    override fun onInitialize(scope: EventScope) {
        super.onInitialize(scope)
        scope.registerAchievementUpdatedEvent { _ ->
            updateState()
        }
        scope.registerAchievementUnlockedEvent { _ ->
            updateState()
        }
        scope.registerAchievementStageUnlockedEvent { _ ->
            updateState()
        }
    }

    override fun onUpdateState() {
        achievementLines.forEach { container.removeChild(it) }

        val trackedAchievements = AchievementHandler.getTrackedAchievements().mapNotNull(::getVisibleAchievement)
        if (trackedAchievements.isEmpty()) {
            if (isEditing) {
                achievementLines = listOf(
                    UIText("Achievement Tracker").constrain {
                        y = SiblingConstraint()
                        width = ScaledTextConstraint(scale)
                        height = TextAspectConstraint()
                    } childOf container
                )
            } else {
                achievementLines = emptyList()
            }
            return
        }

        achievementLines = trackedAchievements.map { achievement ->
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
                AchievementDifficulty.EASY -> UIScheme.easyDifficultyColor
                AchievementDifficulty.MEDIUM -> UIScheme.mediumDifficultyColor
                AchievementDifficulty.HARD -> UIScheme.hardDifficultyColor
                AchievementDifficulty.VERY_HARD -> UIScheme.veryHardDifficultyColor
                AchievementDifficulty.IMPOSSIBLE -> UIScheme.impossibleDifficultyColor
            }

            val current = achievement.currentProgress
            val target = achievement.targetProgress
            val percentage = if (target > 0L) (current.toFloat() / target.toFloat() * 100).toInt() else 0

            val progressText = if (achievement.isCompleted) {
                "Completed!"
            } else {
                "${current.compact()}/${target.compact()} $GRAY($percentage%)"
            }

            UIText("$name: $YELLOW$progressText").constrain {
                y = SiblingConstraint()
                width = ScaledTextConstraint(scale)
                height = TextAspectConstraint()
                this.color = color.toConstraint()
            } childOf container
        }
    }
}
