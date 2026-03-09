package cloud.glitchdev.rfu.gui.components.achievement

import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.gui.UIScheme
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
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint

class Achievement(
    val achievement : IAchievement
) : UIRoundedRectangle(5f) {
    private val padding = 5f

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = UIScheme.achievementBgColorOpaque.toConstraint()
        }

        val container = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - (padding*2).pixels()
            height = ChildBasedSizeConstraint()
        } childOf this

        UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = padding.pixels()
        } childOf container

        val displayName = if (achievement is IStageAchievement && !achievement.isCompleted) {
            achievement.getStageName(achievement.currentStage) ?: achievement.name
        } else {
            achievement.name
        }

        UIText(displayName).constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = ScaledTextConstraint(1f)
            height = TextAspectConstraint()
        } childOf container

        DifficultyDisplay(achievement.difficulty).constrain {
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
        } childOf container

        AchievementProgress(achievement).constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf container

        UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = padding.pixels()
        } childOf container
    }
}