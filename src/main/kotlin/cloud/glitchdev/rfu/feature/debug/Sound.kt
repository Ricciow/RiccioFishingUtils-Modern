package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.arguments.StringListArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.core.registries.BuiltInRegistries

object Sound : AbstractCommand("sound") {
    override val description: String = "Plays a sound using the RFU Sounds utility."

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        val allSounds = BuiltInRegistries.SOUND_EVENT.keySet().map { it.toString() }

        builder
            .then(
                arg("id", StringListArgumentType(allSounds))
                    .then(
                        arg("pitch", FloatArgumentType.floatArg())
                            .executes { context ->
                                if (!DevSettings.devMode) {
                                    context.source.sendFeedback(
                                        TextUtils.rfuLiteral(
                                            "Must have developer mode on to use this feature!",
                                            TextStyle(TextColor.RED, TextEffects.BOLD)
                                        )
                                    )
                                    return@executes 1
                                }

                                val id = StringArgumentType.getString(context, "id")
                                val pitch = FloatArgumentType.getFloat(context, "pitch")

                                Sounds.playSound(id, pitch)

                                context.source.sendFeedback(
                                    TextUtils.rfuLiteral("Played sound: ${TextColor.YELLOW}$id")
                                )

                                1
                            }
                    )
                    .executes { context ->
                        if (!DevSettings.devMode) {
                            context.source.sendFeedback(
                                TextUtils.rfuLiteral(
                                    "Must have developer mode on to use this feature!",
                                    TextStyle(TextColor.RED, TextEffects.BOLD)
                                )
                            )
                            return@executes 1
                        }

                        val id = context.getArgument("id", String::class.java)

                        Sounds.playSound(id)

                        context.source.sendFeedback(
                            TextUtils.rfuLiteral("Played sound: ${TextColor.YELLOW}$id")
                        )

                        1
                    }
            )
    }
}