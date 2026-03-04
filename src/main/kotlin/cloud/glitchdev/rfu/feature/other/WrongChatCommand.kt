package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerSendChatEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerSendCommandEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

@RFUFeature
@Command
object WrongChatCommand : AbstractCommand("wc"), Feature {
    val chatTypes : List<String> = listOf("ac", "pc", "gc", "oc", "cc")
    var lastMessage : String? = null

    override fun onInitialize() {
        registerSendChatEvent { message ->
            lastMessage = message
            true
        }

        registerSendCommandEvent { command ->
            val chatType = chatTypes.find { command.startsWith("$it ") }

            if(chatType != null) {
                lastMessage = command.substringAfter("$chatType ")
            }

            true
        }
    }

    override val description: String = "Resends your last message on a selected chat or dm to an user"

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            arg("chat", StringListArgumentType(chatTypes, exclusive = false))
                .executes { context ->
                    if(lastMessage == null) {
                        context.source.sendFeedback(TextUtils.rfuLiteral("You haven't sent any messages!", TextColor.LIGHT_RED))
                        return@executes 1
                    }

                    val chat = StringArgumentType.getString(context, "chat")
                    if(chatTypes.contains(chat)) {
                        Chat.sendCommand("$chat $lastMessage")
                    } else {
                        Chat.sendCommand("w $chat $lastMessage")
                    }

                    return@executes 1
                }
        ).executes { context ->
            context.source.sendFeedback(TextUtils.rfuLiteral("You must select a chat!", TextColor.LIGHT_RED))
            return@executes 1
        }
    }
}