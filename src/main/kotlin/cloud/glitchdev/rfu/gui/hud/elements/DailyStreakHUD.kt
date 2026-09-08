package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.DailyStreakSettings
import cloud.glitchdev.rfu.constants.text.TextColor.GOLD
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.WHITE
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.data.streak.DailyChallenge
import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ArmorEvents.registerArmorChangeEvent
import cloud.glitchdev.rfu.events.managers.DailyStreakEvents.registerStreakUpdatedEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.utils.dsl.compact
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

@HudElement
object DailyStreakHUD : AbstractTextHudElement("dailyStreakDisplay") {
    override val requirement: Boolean
        get() = DailyStreakSettings.dailyStreakEnabled

    override val isElementActive: Boolean
        get() {
            if (!DailyStreakSettings.dailyStreakEnabled) return false
            if (!DailyStreakSettings.dailyStreakHudEnabled) return false
            if (isEditing) return true
            if (!ArmorEvents.currentArmorSet.isWearingFishingArmor) return false
            if (DailyStreakSettings.autoHideCompletedHud) {
                return DailyStreakManager.data.todayChallenges.any { !it.isCompleted }
            }
            return true
        }

    override val renderOnInventory: Boolean = true

    override fun onInitialize() {
        super.onInitialize()
        registerStreakUpdatedEvent {
            updateState()
        }
        registerArmorChangeEvent {
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        text.clearLines()

        if (!enabled) return

        val data = DailyStreakManager.data

        text.addLine("${WHITE}\uE11F${GOLD}${BOLD}Daily Streak: ${YELLOW}${data.currentStreak} Days")

        val canReroll = DailyStreakManager.canReroll()
        val showReroll = isOnInventory && canReroll

        if (data.todayChallenges.isEmpty()) {
            if (isEditing) {
                if (showReroll) {
                    val lineContainer = UIContainer()
                    UIText("${LIGHT_GREEN}✔ Daily Angler: 15/15m").constrain {
                        x = 0.pixels()
                        y = CenterConstraint()
                        width = ScaledTextConstraint(scale)
                        height = TextAspectConstraint()
                    } childOf lineContainer
                    createRerollButton(null, lineContainer)
                    text.addLine(lineContainer)
                } else {
                    text.addLine("${LIGHT_GREEN}✔ Daily Angler: 15/15m")
                }
            }
        } else {
            data.todayChallenges.forEach { challenge ->
                val icon = if (challenge.isCompleted) "${LIGHT_GREEN}✔" else "${LIGHT_RED}✘"
                val target = challenge.getTargetProgress()
                val targetStr = target.compact()
                val currentStr = challenge.currentProgress.compact()
                val progressStr = if (challenge.isCompleted) {
                    "${LIGHT_GREEN}$targetStr/$targetStr"
                } else {
                    "${YELLOW}$currentStr/$targetStr"
                }
                val lineText = "$icon ${GOLD}${challenge.getTitle()}: $progressStr"

                if (showReroll && !challenge.isCompleted) {
                    val lineContainer = UIContainer().constrain {
                        width = BoundingBoxConstraint()
                        height = ChildBasedMaxSizeConstraint()
                    }
                    UIText(lineText).constrain {
                        x = 0.pixels
                        y = 0.pixels
                        width = ScaledTextConstraint(scale)
                        height = TextAspectConstraint()
                    } childOf lineContainer
                    createRerollButton(challenge, lineContainer)
                    text.addLine(lineContainer)
                } else {
                    text.addLine(lineText)
                }
            }
        }
    }

    private fun createRerollButton(challenge: DailyChallenge?, parent: UIContainer) {
        val iconSize = 7.5f * scale
        val rerollBtn = UIContainer().constrain {
            x = SiblingConstraint(3f * scale)
            y = 0.pixels()
            width = iconSize.pixels()
            height = iconSize.pixels()
        } childOf parent

        val refreshImage = UIImage.ofResource("/assets/rfu/ui/refresh.png").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent()
            height = 100.percent()
            color = Color(200, 200, 200).toConstraint()
        } childOf rerollBtn

        rerollBtn.onMouseEnter {
            refreshImage.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, Color(255, 255, 255).toConstraint())
            }
        }.onMouseLeave {
            refreshImage.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, Color(200, 200, 200).toConstraint())
            }
        }.onMouseClick { event ->
            event.stopPropagation()
            if (challenge != null) {
                DailyStreakManager.rerollChallenge(challenge.id)
            }
        }
    }
}
