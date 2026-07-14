package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.BobberManager
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.text
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.world.phys.Vec3
import java.awt.Color

@RFUFeature
object Bobbers : SimpleCommand("bobbers"), Feature {
    override val description: String = "Toggles rendering info on top of tracked bobbers"

    private var renderAll = false

    override fun onInitialize() {
        registerRenderEvent { context ->
            if (!DevSettings.devMode) return@registerRenderEvent
            if (!renderAll) return@registerRenderEvent

            val active = BobberManager.getActiveBobbers()
            val removed = BobberManager.getRecentlyRemovedBobbers()
            if (active.isEmpty() && removed.isEmpty()) return@registerRenderEvent

            Render3D.draw(context) {
                // Render active bobbers
                for (bobber in active) {
                    val lines = listOf(
                        "§a[Active Bobber]",
                        "§eID: ${bobber.entityId}",
                        "§eOwner: ${bobber.ownerName ?: "Unknown"}",
                        "§7UUID: ${bobber.ownerUUID ?: "N/A"}"
                    )
                    renderLines(bobber.lastPos, lines)
                }

                // Render recently removed bobbers
                for (bobber in removed) {
                    val lines = listOf(
                        "§c[Removed Bobber]",
                        "§eID: ${bobber.entityId}",
                        "§eOwner: ${bobber.ownerName ?: "Unknown"}",
                        "§7UUID: ${bobber.ownerUUID ?: "N/A"}"
                    )
                    renderLines(bobber.lastPos, lines)
                }
            }
        }
    }

    private fun LevelRenderContext.renderLines(baseLoc: Vec3, lines: List<String>) {
        val startY = baseLoc.y + (lines.size - 1) * 0.175
        lines.forEachIndexed { index, lineText ->
            text {
                location = Vec3(baseLoc.x, startY - index * 0.35, baseLoc.z)
                text = lineText
                color = Color.WHITE
                scale = 0.025f
                seeThrough = true
                backgroundOpacity = 0.4f
            }
        }
    }

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

        renderAll = !renderAll

        context.source.sendFeedback(
            TextUtils.rfuLiteral(
                "Tracked Bobber rendering: ${if (renderAll) "§aENABLED" else "§cDISABLED"}",
                TextStyle(TextColor.YELLOW)
            )
        )

        return 1
    }
}
