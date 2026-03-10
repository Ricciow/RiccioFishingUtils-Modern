package cloud.glitchdev.rfu.feature.debug.achievement

import cloud.glitchdev.rfu.utils.command.AbstractCommand

object AchievementDebug : AbstractCommand("achievement") {
    override val description: String = "Commands for debugging achievements."

    init {
        append(AchievementComplete)
        append(AchievementCompleteAll)
        append(AchievementReset)
        append(AchievementResetAll)
        append(AchievementProgress)
        append(AchievementStage)
        append(AchievementAdvanceStage)
    }
}
