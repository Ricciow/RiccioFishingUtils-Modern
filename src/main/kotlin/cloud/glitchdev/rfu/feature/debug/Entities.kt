package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Entities : AbstractCommand("entities") {
    override val description: String = "Commands for entity debugging (sb, bobbers, normal)"

    init {
        append(Sb)
        append(Bobbers)
        append(Normal)
    }

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder
            .executes { context ->
                Normal.executeToggleAll(context)
            }
            .then(
                arg("render", BoolArgumentType.bool())
                    .executes { context ->
                        Normal.executeSetAll(context)
                    }
            )
            .then(
                lit("range")
                    .executes { context ->
                        Normal.executeShowRange(context)
                    }
                    .then(
                        lit("clear")
                            .executes { context ->
                                Normal.executeClearRange(context)
                            }
                    )
                    .then(
                        arg("range", DoubleArgumentType.doubleArg(0.0))
                            .executes { context ->
                                Normal.executeSetRange(context)
                            }
                            .then(
                                arg("render", BoolArgumentType.bool())
                                    .executes { context ->
                                        Normal.executeSetRange(context)
                                    }
                            )
                    )
            )
            .then(
                arg("range", DoubleArgumentType.doubleArg(0.0))
                    .executes { context ->
                        Normal.executeSetRange(context)
                    }
            )
    }
}

