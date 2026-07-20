package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.text
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.world.phys.Vec3
import java.awt.Color

@RFUFeature
object DebugText : Feature, AbstractCommand("text") {
    override val description: String = "Spawns debug text in the 3D world"

    data class FloatingText(
        val message: String,
        val location: Vec3,
        val color: Color,
        val expiresAt: Long,
        val scale: Float,
        val seeThrough: Boolean,
        val scaleWithDistance: Boolean
    )

    private val activeTexts = mutableListOf<FloatingText>()

    override fun onInitialize() {
        registerRenderEvent { context ->
            if (!DevSettings.devMode) return@registerRenderEvent

            val now = System.currentTimeMillis()
            activeTexts.removeAll { it.expiresAt < now }

            if (activeTexts.isEmpty()) return@registerRenderEvent

            Render3D.draw(context) {
                for (t in activeTexts) {
                    text {
                        location = t.location
                        text = t.message
                        color = t.color
                        scale = t.scale
                        seeThrough = t.seeThrough
                        scaleWithDistance = t.scaleWithDistance
                        backgroundOpacity = 0.4f
                    }
                }
            }
        }
    }

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder
            .then(
                arg("message", StringArgumentType.string())
                    .executes { context -> executeSpawn(context) }
                    .then(
                        arg("durationSeconds", IntegerArgumentType.integer(1, 3600))
                            .executes { context -> executeSpawn(context) }
                            .then(
                                arg("scale", FloatArgumentType.floatArg(0.001f, 10f))
                                    .executes { context -> executeSpawn(context) }
                                    .then(
                                        arg("seeThrough", BoolArgumentType.bool())
                                            .executes { context -> executeSpawn(context) }
                                            .then(
                                                arg("scaleWithDistance", BoolArgumentType.bool())
                                                    .executes { context -> executeSpawn(context) }
                                            )
                                    )
                            )
                    )
            )
            .then(
                lit("clear")
                    .executes { context ->
                        activeTexts.clear()
                        context.source.sendFeedback(
                            TextUtils.rfuLiteral(
                                "Cleared all debug texts.",
                                TextStyle(TextColor.LIGHT_GREEN)
                            )
                        )
                        1
                    }
            )
    }

    private fun executeSpawn(context: CommandContext<FabricClientCommandSource>): Int {

        val message = StringArgumentType.getString(context, "message").replace("\\n", "\n")

        val durationSeconds = try {
            IntegerArgumentType.getInteger(context, "durationSeconds")
        } catch (e: IllegalArgumentException) {
            10
        }

        val scale = try {
            FloatArgumentType.getFloat(context, "scale")
        } catch (e: IllegalArgumentException) {
            0.025f
        }

        val seeThrough = try {
            BoolArgumentType.getBool(context, "seeThrough")
        } catch (e: IllegalArgumentException) {
            false
        }

        val scaleWithDistance = try {
            BoolArgumentType.getBool(context, "scaleWithDistance")
        } catch (e: IllegalArgumentException) {
            false
        }

        val cam = Render3D.camera
        val camPos = cam.position()
        val forward = cam.forwardVector()
        val lookVec = Vec3(forward.x().toDouble(), forward.y().toDouble(), forward.z().toDouble())
        val spawnLoc = camPos.add(lookVec.scale(3.0))

        val expiresAt = System.currentTimeMillis() + (durationSeconds * 1000L)
        activeTexts.add(
            FloatingText(
                message = message,
                location = spawnLoc,
                color = Color.YELLOW,
                expiresAt = expiresAt,
                scale = scale,
                seeThrough = seeThrough,
                scaleWithDistance = scaleWithDistance
            )
        )

        context.source.sendFeedback(
            TextUtils.rfuLiteral(
                "Spawned debug text '${message.replace("\n", "\\n")}' at look position for ${durationSeconds}s (scale=$scale, seeThrough=$seeThrough, scaleWithDistance=$scaleWithDistance)",
                TextStyle(TextColor.LIGHT_GREEN)
            )
        )

        return 1
    }
}
