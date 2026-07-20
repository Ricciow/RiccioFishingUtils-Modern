package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.data.mob.MobManager
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.text
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.awt.Color

@RFUFeature
object Sb : SimpleCommand("sb"), Feature {
    override val description: String = "Toggles rendering info on top of Skyblock entities"

    private var renderAll = false

    override fun onInitialize() {
        registerRenderEvent { context ->
            if (!DevSettings.devMode) return@registerRenderEvent
            if (!renderAll) return@registerRenderEvent

            val entities = MobManager.getEntities()
            if (entities.isEmpty()) return@registerRenderEvent

            Render3D.draw(context) {
                val tickDelta = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
                for (sbEntity in entities) {
                    if (sbEntity.isRemoved()) continue

                    val entityPos = sbEntity.modelEntity.getPosition(tickDelta)
                    val baseLoc = entityPos.add(0.0, sbEntity.modelEntity.bbHeight.toDouble() + 0.5, 0.0)

                    val lines = mutableListOf<String>()
                    val scName = sbEntity.getName() ?: "Unknown"
                    lines.add("§a$scName §e(${sbEntity.modelEntity.type.toShortString()}) §7[Model ID: ${sbEntity.modelEntity.id}, Tag ID: ${sbEntity.nameTagEntity.id}]")

                    val shurikenStr = if (sbEntity.isShurikened) " §b[Shurikened]" else ""
                    lines.add("§cHP: ${sbEntity.health}/${sbEntity.maxHealth}$shurikenStr")

                    val origin = sbEntity.originBobber
                    if (origin != null) {
                        lines.add("§9Bobber: ${origin.entityId} (Owner: ${origin.ownerName ?: "Unknown"})")
                    }

                    text {
                        location = baseLoc
                        text = lines.joinToString("\n")
                        color = Color.WHITE
                        scale = 0.025f
                        seeThrough = true
                        backgroundOpacity = 0.4f
                    }
                }
            }
        }
    }

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        renderAll = !renderAll

        context.source.sendFeedback(
            TextUtils.rfuLiteral(
                "Skyblock Entity rendering: ${if (renderAll) "§aENABLED" else "§cDISABLED"}",
                TextStyle(TextColor.YELLOW)
            )
        )

        return 1
    }
}
