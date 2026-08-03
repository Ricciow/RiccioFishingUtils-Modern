package cloud.glitchdev.rfu.gui.components.achievement

import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.data.achievements.AchievementHandler
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
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
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.toConstraint

class Achievement(
    val achievement: IAchievement,
    val wasHovered: Boolean = false,
    initialViewingStage: Int? = null
) : UIRoundedRectangle(5f) {
    private val padding = UIScheme.pfCardInnerPadding

    var viewingStage: Int = initialViewingStage ?: if (achievement is IStageAchievement) {
        if (achievement.isCompleted) achievement.targetStage else achievement.currentStage.coerceIn(1, achievement.targetStage)
    } else 1

    init {
        create()
    }

    fun create() {
        val borderWidth = UIScheme.pfCardBorderWidth
        this.constrain {
            color = (if (wasHovered) UIScheme.pfCardBorderHovered else UIScheme.pfCardBorder).toConstraint()
            height = BoundingBoxConstraint() + borderWidth.pixels()
        }

        val innerBg = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = borderWidth.pixels()
            width = 100.percent() - (borderWidth * 2).pixels()
            height = BoundingBoxConstraint() + padding.pixels()
            color = UIScheme.pfCardBg.toConstraint()
        } childOf this

        val container = UIContainer().constrain {
            x = padding.pixels()
            y = padding.pixels()
            width = 100.percent() - (padding * 2).pixels()
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

        star.setHidden(!wasHovered && !AchievementHandler.isTracked(achievement.id))

        this.onMouseEnter {
            star.setHidden(false)
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorderHovered.toConstraint())
            }
        }.onMouseLeave {
            star.setHidden(!AchievementHandler.isTracked(achievement.id))
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorder.toConstraint())
            }
        }

        star.onMouseClick { event ->
            event.stopPropagation()
            val isTracked = AchievementHandler.isTracked(achievement.id)
            AchievementHandler.setTracked(achievement.id, !isTracked)
            star.setText(if (!isTracked) "⭐" else "☆")
            star.constrain {
                color = (if (!isTracked) UIScheme.trackedStarColor else UIScheme.untrackedStarColor).toConstraint()
            }
            AchievementTrackerDisplay.updateState()
        }

        fun getDisplayName(): String {
            return if (achievement is IStageAchievement) {
                achievement.getStageName(viewingStage) ?: achievement.name
            } else {
                achievement.name
            }
        }

        fun getDisplayDifficulty() = if (achievement is IStageAchievement) {
            achievement.getStageDifficulty(viewingStage) ?: achievement.difficulty
        } else {
            achievement.difficulty
        }

        fun getDisplayDescription(): String {
            val rawDesc = if (achievement.isCompleted || achievement.type != AchievementType.SECRET) {
                achievement.description
            } else {
                "???"
            }

            return if (achievement is IStageAchievement) {
                if (achievement.type == AchievementType.SECRET && !achievement.isCompleted && viewingStage > achievement.currentStage) {
                    "???"
                } else {
                    achievement.getStageDescription(viewingStage) ?: rawDesc
                }
            } else {
                rawDesc
            }
        }

        val nameText = UIText(getDisplayName()).constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(1.1f)
            height = TextAspectConstraint()
        } childOf topContainer

        if (DevSettings.devMode) {
            UIText(achievement.id).constrain {
                x = 0.pixels()
                y = SiblingConstraint()
                width = ScaledTextConstraint(0.8f)
                height = TextAspectConstraint()
                color = UIScheme.achievementIdColor.toConstraint()
            } childOf container
        }

        val difficultyDisplay = DifficultyDisplay(getDisplayDifficulty()).constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = ScaledTextConstraint(1f)
            height = TextAspectConstraint()
        } childOf container

        val descText = UIWrappedText(getDisplayDescription()).constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = 100.percent()
            height = TextWrappingConstraint()
            color = UIScheme.achievementDescriptionColor.toConstraint()
        } childOf container

        val arrowSection = UIContainer().constrain {
            x = 0.pixels
            y = SiblingConstraint()
            width = 100.percent()
            height = 9.pixels
        } childOf container

        val bottomSection = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfCardSmallPadding)
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf container

        val isStage = achievement is IStageAchievement
        val stageAch = achievement as? IStageAchievement

        lateinit var leftArrow: UIText
        lateinit var rightArrow: UIText

        if (isStage) {
            leftArrow = UIText("◀").constrain {
                x = 0.pixels()
                y = CenterConstraint()
                width = ScaledTextConstraint(1f)
                height = TextAspectConstraint()
            } childOf arrowSection

            rightArrow = UIText("▶").constrain {
                x = 0.pixels(true)
                y = CenterConstraint()
                width = ScaledTextConstraint(1f)
                height = TextAspectConstraint()
            } childOf arrowSection
        }

        val progressWrapper = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf bottomSection

        var currentProgressComponent: AchievementProgress = AchievementProgress(achievement, if (isStage) viewingStage else null).constrain {
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf progressWrapper

        if (isStage && stageAch != null) {
            fun updateStageNavColors() {
                val canGoLeft = viewingStage > 1
                val canGoRight = viewingStage < stageAch.targetStage

                leftArrow.constrain {
                    color = (if (canGoLeft) UIScheme.primaryTextColor else UIScheme.disabledTextColor).toConstraint()
                }
                rightArrow.constrain {
                    color = (if (canGoRight) UIScheme.primaryTextColor else UIScheme.disabledTextColor).toConstraint()
                }
            }

            fun updateContent() {
                nameText.setText(getDisplayName())
                difficultyDisplay.updateDifficulty(getDisplayDifficulty())
                descText.setText(getDisplayDescription())
                updateStageNavColors()

                progressWrapper.removeChild(currentProgressComponent)
                currentProgressComponent = AchievementProgress(achievement, viewingStage).constrain {
                    width = 100.percent()
                    height = ChildBasedSizeConstraint()
                } childOf progressWrapper
            }

            leftArrow.onMouseEnter {
                if (viewingStage > 1) {
                    leftArrow.constrain { color = UIScheme.selectedTextColor.toConstraint() }
                }
            }.onMouseLeave {
                updateStageNavColors()
            }

            rightArrow.onMouseEnter {
                if (viewingStage < stageAch.targetStage) {
                    rightArrow.constrain { color = UIScheme.selectedTextColor.toConstraint() }
                }
            }.onMouseLeave {
                updateStageNavColors()
            }

            leftArrow.onMouseClick { event ->
                event.stopPropagation()
                if (viewingStage > 1) {
                    viewingStage--
                    updateContent()
                }
            }

            rightArrow.onMouseClick { event ->
                event.stopPropagation()
                if (viewingStage < stageAch.targetStage) {
                    viewingStage++
                    updateContent()
                }
            }

            updateStageNavColors()
        }
    }
}