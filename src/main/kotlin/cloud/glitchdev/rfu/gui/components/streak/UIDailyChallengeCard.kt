package cloud.glitchdev.rfu.gui.components.streak

import cloud.glitchdev.rfu.data.streak.DailyChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeLevel
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeRegistry
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.components.elementa.TextWrappingConstraint
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
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
import java.awt.Color

import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.min

class UIDailyChallengeCard(
    val challenge: DailyChallenge,
    val index: Int,
    val wasHovered: Boolean = false,
    val onRerollClick: ((DailyChallenge) -> Unit)? = null
) : UIRoundedRectangle(5f) {
    private val borderWidth = UIScheme.pfCardBorderWidth
    private val innerPadding = UIScheme.pfCardInnerPadding

    lateinit var titleText: UIText

    init {
        create()
    }

    private fun create() {
        val normalTitleColor = if (challenge.isCompleted) UIScheme.achievementCompleteColor else Color(255, 215, 0)

        this.constrain {
            color = (if (wasHovered) UIScheme.pfCardBorderHovered else UIScheme.pfCardBorder).toConstraint()
            height = BoundingBoxConstraint() + borderWidth.pixels()
        }.onMouseEnter {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorderHovered.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleHoverColor.toConstraint())
            }
        }.onMouseLeave {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorder.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, normalTitleColor.toConstraint())
            }
        }

        val innerBg = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = borderWidth.pixels()
            width = 100.percent() - (borderWidth * 2).pixels()
            height = BoundingBoxConstraint() + (innerPadding * 2).pixels()
            color = UIScheme.pfCardBg.toConstraint()
        } childOf this

        val innerContainer = UIContainer().constrain {
            x = innerPadding.pixels()
            y = innerPadding.pixels()
            width = 100.percent() - (innerPadding * 2).pixels()
            height = BoundingBoxConstraint()
        } childOf innerBg

        val header = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = max(ChildBasedMaxSizeConstraint(), 20.pixels())
        } childOf innerContainer

        val baseId = challenge.id.substringBeforeLast("_")
        val baseDef = ChallengeRegistry.getChallenge(baseId)
        val levelStr = challenge.level.formattedName

        titleText = UIText("${index + 1}. ${challenge.title} §7[$levelStr§7]").constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(1.1f)
            height = TextAspectConstraint()
            color = (if (wasHovered) UIScheme.pfCardTitleHoverColor else normalTitleColor).toConstraint()
        } childOf header

        val statusStr = if (challenge.isCompleted) "✔ COMPLETED" else "${challenge.currentProgress} / ${challenge.targetProgress}"
        val statusColor = if (challenge.isCompleted) UIScheme.achievementCompleteColor else Color(255, 170, 0)

        val rightContainer = UIContainer().constrain {
            x = 0.pixels(true)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        } childOf header

        if (!challenge.isCompleted && baseDef?.isMandatoryBase != true && DailyStreakManager.canReroll()) {
            val rerollBtn = UIRoundedRectangle(3f).constrain {
                x = 0.pixels()
                y = CenterConstraint()
                width = ChildBasedSizeConstraint() + 8.pixels()
                height = ChildBasedSizeConstraint() + 4.pixels()
                color = Color(50, 50, 60, 200).toConstraint()
            } childOf rightContainer

            val rerollText = UIText("🎲 Reroll").constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                width = ScaledTextConstraint(0.85f)
                height = TextAspectConstraint()
                color = Color(220, 220, 255).toConstraint()
            } childOf rerollBtn

            rerollBtn.onMouseEnter {
                rerollBtn.animate {
                    setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, Color(70, 70, 90, 240).toConstraint())
                }
                rerollText.animate {
                    setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, Color(255, 255, 255).toConstraint())
                }
            }.onMouseLeave {
                rerollBtn.animate {
                    setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, Color(50, 50, 60, 200).toConstraint())
                }
                rerollText.animate {
                    setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, Color(220, 220, 255).toConstraint())
                }
            }.onMouseClick {
                onRerollClick?.invoke(challenge)
            }

            UIText(statusStr).constrain {
                x = SiblingConstraint(8f)
                y = CenterConstraint()
                width = ScaledTextConstraint(1.0f)
                height = TextAspectConstraint()
                color = statusColor.toConstraint()
            } childOf rightContainer
        } else {
            UIText(statusStr).constrain {
                x = 0.pixels()
                y = CenterConstraint()
                width = ScaledTextConstraint(1.0f)
                height = TextAspectConstraint()
                color = statusColor.toConstraint()
            } childOf rightContainer
        }

        UIWrappedText(challenge.description).constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfCardSmallPadding)
            width = 100.percent()
            height = TextWrappingConstraint()
            color = UIScheme.pfCardDescriptionColor.toConstraint()
        } childOf innerContainer

        val progressBarTrack = UIRoundedRectangle(3f).constrain {
            x = 0.pixels()
            y = SiblingConstraint(UIScheme.pfCardSmallPadding + 2f)
            width = 100.percent()
            height = 6.pixels()
            color = Color(20, 20, 25, 220).toConstraint()
        } childOf innerContainer

        val progressPct = challenge.progressPercent
        if (progressPct > 0f) {
            val fillColor = if (challenge.isCompleted) UIScheme.achievementCompleteColor else UIScheme.primaryColor
            UIRoundedRectangle(3f).constrain {
                x = 0.pixels()
                y = 0.pixels()
                width = (progressPct * 100).percent()
                height = 100.percent()
                color = fillColor.toConstraint()
            } childOf progressBarTrack
        }
    }
}
