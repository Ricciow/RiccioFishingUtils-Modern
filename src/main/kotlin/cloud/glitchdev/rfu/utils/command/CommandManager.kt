package cloud.glitchdev.rfu.utils.command

import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.utils.TextUtils.rfuLiteral
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

@AutoRegister
object CommandManager : RegisteredEvent{
    val commands : MutableList<AbstractCommand> = mutableListOf()

    fun addCommand(command : AbstractCommand) {
        commands.add(command)
    }

    override fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            commands.forEach {
                dispatcher.register(it.build())
            }
        }
    }

    @Command
    object HelpCommand : AbstractCommand("rfuhelp") {
        override val description: String = "Gives information about other commands"

        override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
            builder
                .then(
                    arg("command", StringListArgumentType(commands.map { it.name }))
                        .executes { context ->
                            val commandName = StringArgumentType.getString(context, "command")

                            val command = commands.find { it.name == commandName }

                            if (command != null) {
                                val text = rfuLiteral("Command info:", TextStyle(YELLOW))
                                    .append(commandText(command))

                                context.source.sendFeedback(text)
                            }
                            else {
                                context.source.sendFeedback(
                                    rfuLiteral("Unable to find command $commandName", TextStyle(RED))
                                )
                            }


                            1
                        }
                )
        }

        fun commandText(command: AbstractCommand, spacing : Int = 0) : MutableComponent {
            val text = Component.literal("\n${" ".repeat(spacing)}- $GOLD${command.name}: $WHITE${command.description}")

            if(command.subCommands.isNotEmpty()) {
                text.append("\n${" ".repeat(spacing)}${YELLOW}- Sub Commands:")
                command.subCommands.forEach { command ->
                    text.append(commandText(command, spacing + 2))
                }
            }

            return text
        }
    }
}