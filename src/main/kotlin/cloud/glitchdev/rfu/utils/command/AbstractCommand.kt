package cloud.glitchdev.rfu.utils.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

abstract class AbstractCommand(val name: String) {
    val subCommands = mutableListOf<AbstractCommand>()

    abstract val description : String

    protected open fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {}

    fun register() {
        CommandManager.addCommand(this)
    }

    fun build(): LiteralArgumentBuilder<FabricClientCommandSource> {
        val builder = lit(name)

        for (subCommand in subCommands) {
            builder.then(subCommand.build())
        }

        build(builder)

        return builder
    }

    protected fun lit(literal: String): LiteralArgumentBuilder<FabricClientCommandSource> {
        return LiteralArgumentBuilder.literal(literal)
    }

    protected fun <T> arg(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<FabricClientCommandSource, T> {
        return RequiredArgumentBuilder.argument(name, type)
    }

    fun append(command: AbstractCommand): AbstractCommand {
        subCommands.add(command)
        return this
    }
}