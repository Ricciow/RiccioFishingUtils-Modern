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
    val achievement: IAchievement
) : UIContainer() {
    init {
        val textContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = ChildBasedMaxSizeConstraint()
        } childOf this

        if(achievement.isCompleted) {
            UIText("Completed!").constrain {
                x = 0.pixels()
                y = CenterConstraint()
                width = ScaledTextConstraint(1f)
                height = TextAspectConstraint()
                color = UIScheme.achievementCompleteColor.toConstraint()
            } childOf textContainer
        } else if(achievement is IStageAchievement) {
            UIText("Stage ${achievement.currentStage}").constrain {
                x = 0.pixels()
                y = CenterConstraint()
                width = ScaledTextConstraint(1f)
                height = TextAspectConstraint()
            } childOf textContainer
        }

        val currentStr = achievement.currentProgress.compact()
        val targetStr = achievement.targetProgress.compact()
        val progressText = if(achievement.isCompleted) "${TextColor.LIGHT_GREEN}✔" else "$currentStr/$targetStr"
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
            color = (if(achievement.progress != 1f) UIScheme.achievementIncompleteColor else UIScheme.achievementCompleteColor).toConstraint()
        } childOf progressBackground
    }
}
