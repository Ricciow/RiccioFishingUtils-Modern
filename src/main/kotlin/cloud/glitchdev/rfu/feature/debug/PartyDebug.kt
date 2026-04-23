package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object PartyDebug : SimpleCommand("party") {
    override val description: String = "Shows current party information."

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        if (!DevSettings.devMode) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Must have developer mode on to use this feature!",
                    TextStyle(TextColor.RED, TextEffects.BOLD)
                )
            )
            return 1
        }

        val message = Component.literal("${TextColor.CYAN}${TextEffects.STRIKE}-----------------------------------------------------\n")
        message.append(TextUtils.rfuLiteral("Party Debug Info:\n", TextStyle(TextColor.GOLD, TextEffects.BOLD)))
        message.append(Component.literal("${TextColor.YELLOW}In Party: ${if (Party.inParty) "${TextColor.LIGHT_GREEN}Yes" else "${TextColor.LIGHT_RED}No"}\n"))
        message.append(Component.literal("${TextColor.YELLOW}Is Leader: ${if (Party.isLeader) "${TextColor.LIGHT_GREEN}Yes" else "${TextColor.LIGHT_RED}No"}\n"))
        message.append(Component.literal("${TextColor.YELLOW}Is All Invite: ${if (Party.isAllInvite) "${TextColor.LIGHT_GREEN}Yes" else "${TextColor.LIGHT_RED}No"}\n"))
        message.append(Component.literal("${TextColor.YELLOW}Members (${Party.members.size}):\n"))

        if (Party.members.isEmpty()) {
            message.append(Component.literal("${TextColor.GRAY}  No members\n"))
        } else {
            Party.members.forEach { (name, role) ->
                message.append(Component.literal("${TextColor.GRAY}  - ${TextColor.WHITE}$name ${TextColor.GRAY}($role)\n"))
            }
        }
        message.append(Component.literal("${TextColor.YELLOW}Requested User: ${TextColor.WHITE}${Party.requestedUser ?: "None"}\n"))
        message.append(Component.literal("${TextColor.CYAN}${TextEffects.STRIKE}-----------------------------------------------------"))

        Chat.sendMessage(message)
        return 1
    }
}
