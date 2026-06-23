package cloud.glitchdev.rfu.gui.components.achievement

import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.data.achievements.AchievementHandler
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.TextWrappingConstraint
import cloud.glitchdev.rfu.gui.hud.elements.AchievementTrackerDisplay
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.dsl.plus
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate

class Achievement(
    val achievement : IAchievement
) : UIRoundedRectangle(5f) {
    private val padding = UIScheme.pfCardInnerPadding

    init {
        create()
    }

    fun create() {
        val borderWidth = UIScheme.pfCardBorderWidth
        this.constrain {
            color = UIScheme.pfCardBorder.toConstraint()
            height = BoundingBoxConstraint() + (borderWidth).pixels()
        }.onMouseEnter {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorderHovered.toConstraint())
            }
        }.onMouseLeave {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorder.toConstraint())
            }
        }

        val innerBg = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = borderWidth.pixels()
            width = 100.percent() - (borderWidth * 2).pixels()
            height = BoundingBoxConstraint() + (padding * 2).pixels()
            color = UIScheme.pfCardBg.toConstraint()
        } childOf this

        val container = UIContainer().constrain {
            x = padding.pixels()
            y = padding.pixels()
            width = 100.percent() - (padding*2).pixels()
            height = BoundingBoxConstraint()
        } childOf innerBg

        val topContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = ChildBasedMaxSizeConstraint()
        } childOf container

        val star = UIText(if (AchievementHandler.isTracked(achievement.id)) "⭐" else "☆").constrain {
            x = 0.pixels(true)
            y = CenterConstraint()
            width = ScaledTextConstraint(1.1f)
            height = TextAspectConstraint()
            color = (if (AchievementHandler.isTracked(achievement.id)) UIScheme.trackedStarColor else UIScheme.untrackedStarColor).toConstraint()
        } childOf topContainer

        star.setHidden(!AchievementHandler.isTracked(achievement.id))

        this.onMouseEnter {
            star.setHidden(false)
        }
        this.onMouseLeave {
            star.setHidden(!AchievementHandler.isTracked(achievement.id))
        }

        star.onMouseClick {
            val isTracked = AchievementHandler.isTracked(achievement.id)
            AchievementHandler.setTracked(achievement.id, !isTracked)
            star.setText(if (!isTracked) "⭐" else "☆")
            star.constrain {
                color = (if (!isTracked) UIScheme.trackedStarColor else UIScheme.untrackedStarColor).toConstraint()
            }
            AchievementTrackerDisplay.updateState()
        }

        val displayName = if (achievement is IStageAchievement && !achievement.isCompleted) {
            achievement.getStageName(achievement.currentStage) ?: achievement.name
        } else {
            achievement.name
        }


        UIText(displayName).constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(1.1f)
            height = TextAspectConstraint()
        } childOf topContainer

        val displayDifficulty = if (achievement is IStageAchievement && !achievement.isCompleted) {
            achievement.getStageDifficulty(achievement.currentStage) ?: achievement.difficulty
        } else {
            achievement.difficulty
        }

        DifficultyDisplay(displayDifficulty).constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = ScaledTextConstraint(1f)
            height = TextAspectConstraint()
        } childOf container

        val rawDescription = if(achievement.isCompleted || achievement.type != AchievementType.SECRET) achievement.description else "???"
        val displayDescription = if (achievement is IStageAchievement && !achievement.isCompleted && achievement.type != AchievementType.SECRET) {
            achievement.getStageDescription(achievement.currentStage) ?: rawDescription
        } else {
            rawDescription
        }

        UIWrappedText(displayDescription).constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = 100.percent()
            height = TextWrappingConstraint()
            color = UIScheme.achievementDescriptionColor.toConstraint()
        } childOf container

        AchievementProgress(achievement).constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfCardSmallPadding)
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf container
    }
}