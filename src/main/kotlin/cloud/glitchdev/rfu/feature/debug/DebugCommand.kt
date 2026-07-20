package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.feature.debug.achievement.AchievementDebug
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object DebugCommand : AbstractCommand("rfudebug") {
    override val description: String = "Command used for debugging rfu"

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.requires { DevSettings.devMode }
    }

    init {
        append(Chat)
        append(Title)
        append(Entities)
        append(Sound)
        append(PartyDebug)
        append(AnnouncementDebug)
        append(AchievementDebug)
        append(DebugHotspots.Hotspots)
        append(Reauth)
        append(SkillsDebug)
        append(DebugText)
    }
}