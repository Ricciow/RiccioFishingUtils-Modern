package cloud.glitchdev.rfu.feature.other.ignore

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object AddIgnoreSubCommand : AbstractCommand("add") {
    override val description: String = "Add a user to the ignore list"

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            arg("username", StringArgumentType.word()).executes { context ->
                val username = StringArgumentType.getString(context, "username")
                val entry = IgnoreUtils.getIgnoredEntry()
                if (entry.add(username)) {
                    IgnoreUtils.saveIgnoredEntry(entry)
                    PartyFinderEvents.refreshParties()
                    context.source.sendFeedback(TextUtils.rfuLiteral("Added ${TextColor.GOLD}$username${TextColor.LIGHT_GREEN} to the ignore list.", TextColor.LIGHT_GREEN))
                } else {
                    context.source.sendFeedback(TextUtils.rfuLiteral("${TextColor.GOLD}$username${TextColor.YELLOW} is already in the ignore list.", TextColor.YELLOW))
                }
                1
            }
        )
    }
}
