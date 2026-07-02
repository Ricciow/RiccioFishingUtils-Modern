package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.feature.debug.achievement.AchievementDebug
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command

@Command
object DebugCommand : AbstractCommand("rfudebug") {
    override val description: String = "Command used for debugging rfu"
    init {
        append(Chat)
        append(Title)
        append(SbEntities)
        append(Alive)
        append(Sound)
        append(PartyDebug)
        append(AnnouncementDebug)
        append(AchievementDebug)
        append(DebugHotspots.Hotspots)
        append(Reauth)
        append(SkillsDebug)
    }
}