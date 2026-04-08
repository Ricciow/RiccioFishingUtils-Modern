package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.sphere
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.awt.Color

@RFUFeature
object DebugHotspots : Feature {
    private var showSphere = false

    override fun onInitialize() {
        registerRenderEvent { context ->
            if (!showSphere || !DevSettings.devMode) return@registerRenderEvent

            Render3D.draw(context) {
                for (hotspot in HotSpotEvents.getAllHotspots()) {
                    sphere {
                        location = hotspot.center
                        radius = 25f
                        color = Color(255, 255, 255, 30)
                        borderColor = Color.WHITE
                        lineWidth = 2.0f
                        filled = true
                    }
                }
            }
        }
    }

    object Hotspots : SimpleCommand("hotspots") {
        override val description: String = "Toggles the debug sphere for hotspots"

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

            showSphere = !showSphere
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Hotspot debug sphere: ${if (showSphere) "§aENABLED" else "§cDISABLED"}",
                    TextStyle(TextColor.YELLOW)
                )
            )

            return 1
        }
    }
}
