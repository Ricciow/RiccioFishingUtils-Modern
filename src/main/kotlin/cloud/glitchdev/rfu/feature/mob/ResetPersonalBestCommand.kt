package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@Command
object ResetPersonalBestCommand : AbstractCommand("rfuresetpb") {
    override val description: String = "Resets a sea creature's personal best kill time."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            arg("name", StringListArgumentType(SeaCreatures.entries.flatMap { listOf(it.scName, it.scDisplayName) }.distinct(), greedy = true, exclusive = false))
                .executes { context ->
                    val name = StringArgumentType.getString(context, "name").trim()
                    val sc = SeaCreatures.entries.find {
                        it.scName.equals(name, ignoreCase = true) ||
                            it.scDisplayName.equals(name, ignoreCase = true)
                    }

                    if (sc == null) {
                        Chat.sendMessage(TextUtils.rfuLiteral("Sea creature not found: $name", TextColor.LIGHT_RED))
                        return@executes 0
                    }

                    if (!CatchTracker.catchHistory.resetKillTime(sc)) {
                        Chat.sendMessage(TextUtils.rfuLiteral("No personal best kill time saved for ${sc.scDisplayName}.", TextColor.LIGHT_RED))
                        return@executes 0
                    }

                    CatchTracker.catchesFile.save()
                    Chat.sendMessage(TextUtils.rfuLiteral("Reset ${sc.scDisplayName}'s personal best kill time.", TextColor.LIGHT_GREEN))
                    1
                }
        )
    }
}
