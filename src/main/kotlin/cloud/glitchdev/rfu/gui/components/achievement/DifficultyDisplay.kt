package cloud.glitchdev.rfu.gui.components.achievement

import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.gui.UIScheme
import gg.essential.elementa.components.UIText
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

class DifficultyDisplay(
    val difficulty : AchievementDifficulty
) : UIText() {
    init {
        val string = getStarString()
        this.setText(string.first)
        this.setColor(string.second.toConstraint())
    }

    fun updateDifficulty(newDiff: AchievementDifficulty) {
        val string = getStarString(newDiff)
        this.setText(string.first)
        this.setColor(string.second.toConstraint())
    }

    private fun getStarString(diff: AchievementDifficulty = difficulty) : Pair<String, Color> {
        return when(diff) {
            AchievementDifficulty.EASY -> "⭐☆☆☆☆ Easy" to UIScheme.easyDifficultyColor
            AchievementDifficulty.MEDIUM -> "⭐⭐☆☆☆ Medium" to UIScheme.mediumDifficultyColor
            AchievementDifficulty.HARD -> "⭐⭐⭐☆☆ Hard" to UIScheme.hardDifficultyColor
            AchievementDifficulty.VERY_HARD -> "⭐⭐⭐⭐☆ Very Hard" to UIScheme.veryHardDifficultyColor
            AchievementDifficulty.IMPOSSIBLE -> "⭐⭐⭐⭐⭐ Impossible" to UIScheme.impossibleDifficultyColor
        }
    }
}