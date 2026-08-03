package cloud.glitchdev.rfu.gui.components.achievement

import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.gui.UIScheme
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import cloud.glitchdev.rfu.utils.dsl.compact
import java.awt.Color

class AchievementProgress(
    val achievement: IAchievement,
    val viewingStage: Int? = null
) : UIContainer() {
    init {
        this.constrain {
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        }

        val textContainer = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = ChildBasedMaxSizeConstraint()
        } childOf this

        val stageAch = achievement as? IStageAchievement

        if (stageAch != null) {
            val stageNum = viewingStage ?: stageAch.currentStage

            val isStageCompleted = achievement.isCompleted || stageNum < stageAch.currentStage
            val isStageActive = !achievement.isCompleted && stageNum == stageAch.currentStage
            val isStageLocked = !achievement.isCompleted && stageNum > stageAch.currentStage

            if (isStageCompleted) {
                UIText("Completed!").constrain {
                    x = 0.pixels()
                    y = CenterConstraint()
                    width = ScaledTextConstraint(1f)
                    height = TextAspectConstraint()
                    color = UIScheme.achievementCompleteColor.toConstraint()
                } childOf textContainer
            } else if (isStageActive) {
                UIText("Stage $stageNum").constrain {
                    x = 0.pixels()
                    y = CenterConstraint()
                    width = ScaledTextConstraint(1f)
                    height = TextAspectConstraint()
                } childOf textContainer
            } else if (isStageLocked) {
                UIText("Stage $stageNum ${TextColor.GRAY}(Locked)").constrain {
                    x = 0.pixels()
                    y = CenterConstraint()
                    width = ScaledTextConstraint(1f)
                    height = TextAspectConstraint()
                    color = UIScheme.secondaryTextColor.toConstraint()
                } childOf textContainer
            }

            val progressText = when {
                isStageCompleted -> "${TextColor.LIGHT_GREEN}✔"
                isStageActive -> {
                    val currentStr = achievement.currentProgress.compact()
                    val targetStr = achievement.targetProgress.compact()
                    "$currentStr/$targetStr"
                }
                isStageLocked -> {
                    val stageTarget = stageAch.getStageTargetProgress(stageNum)
                    if (stageTarget != null) {
                        "${TextColor.GRAY}0/${stageTarget.compact()}"
                    } else {
                        "${TextColor.GRAY}Locked 🔒"
                    }
                }
                else -> {
                    val currentStr = achievement.currentProgress.compact()
                    val targetStr = achievement.targetProgress.compact()
                    if (achievement.isCompleted) "${TextColor.LIGHT_GREEN}✔" else "$currentStr/$targetStr"
                }
            }

            UIText(progressText).constrain {
                x = 0.pixels(true)
                y = CenterConstraint()
                width = ScaledTextConstraint(1f)
                height = TextAspectConstraint()
            } childOf textContainer

            val fillPercent = when {
                isStageCompleted -> 1.0f
                isStageActive -> achievement.progress
                isStageLocked -> 0.0f
                else -> achievement.progress
            }

            val progressBackground = UIBlock().constrain {
                x = 0.pixels()
                y = SiblingConstraint(2f)
                width = 100.percent()
                height = 4.pixels()
                color = Color(255, 255, 255, 40).toConstraint()
            } childOf this

            UIBlock().constrain {
                x = 0.pixels()
                y = 0.pixels()
                width = (fillPercent * 100).percent()
                height = 100.percent()
                color = (if (fillPercent != 1f) UIScheme.achievementIncompleteColor else UIScheme.achievementCompleteColor).toConstraint()
            } childOf progressBackground
        } else {
            if (achievement.isCompleted) {
                UIText("Completed!").constrain {
                    x = 0.pixels()
                    y = CenterConstraint()
                    width = ScaledTextConstraint(1f)
                    height = TextAspectConstraint()
                    color = UIScheme.achievementCompleteColor.toConstraint()
                } childOf textContainer
            }

            val currentStr = achievement.currentProgress.compact()
            val targetStr = achievement.targetProgress.compact()
            val progressText = if (achievement.isCompleted) "${TextColor.LIGHT_GREEN}✔" else "$currentStr/$targetStr"

            UIText(progressText).constrain {
                x = 0.pixels(true)
                y = CenterConstraint()
                width = ScaledTextConstraint(1f)
                height = TextAspectConstraint()
            } childOf textContainer

            val progressBackground = UIBlock().constrain {
                x = 0.pixels()
                y = SiblingConstraint(2f)
                width = 100.percent()
                height = 4.pixels()
                color = Color(255, 255, 255, 40).toConstraint()
            } childOf this

            UIBlock().constrain {
                x = 0.pixels()
                y = 0.pixels()
                width = (achievement.progress * 100).percent()
                height = 100.percent()
                color = (if (achievement.progress != 1f) UIScheme.achievementIncompleteColor else UIScheme.achievementCompleteColor).toConstraint()
            } childOf progressBackground
        }
    }
}
