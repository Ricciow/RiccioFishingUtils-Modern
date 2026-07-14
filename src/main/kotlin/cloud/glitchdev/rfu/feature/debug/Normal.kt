package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
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
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Display
import net.minecraft.world.phys.Vec3
import java.awt.Color

@RFUFeature
object Normal : Feature, AbstractCommand("normal") {
    override val description: String = "Toggles or sets rendering of info on top of currently alive entities"

    private var renderAll = false
    private var targetId: Int? = null
    private var filter: String? = null

    override fun onInitialize() {
        registerRenderEvent { context ->
            if (!DevSettings.devMode) return@registerRenderEvent
            if (!renderAll && targetId == null) return@registerRenderEvent

            val world = mc.level ?: return@registerRenderEvent
            val entities = world.entitiesForRendering().filter { it.isAlive }

            val targetEntities = if (targetId != null) {
                entities.filter { it.id == targetId }
            } else if (renderAll) {
                val f = filter
                if (f != null) {
                    entities.filter { entity ->
                        entity.type.toShortString().contains(f, ignoreCase = true) ||
                        entity.type.description.toUnformattedString().contains(f, ignoreCase = true)
                    }
                } else {
                    entities
                }
            } else {
                emptyList()
            }

            if (targetEntities.isEmpty()) return@registerRenderEvent

            Render3D.draw(context) {
                val tickDelta = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
                for (entity in targetEntities) {
                    val entityPos = entity.getPosition(tickDelta)
                    val baseLoc = entityPos.add(0.0, entity.bbHeight.toDouble() + 0.5, 0.0)

                    val lines = mutableListOf<String>()
                    lines.add("§a${entity.name.toUnformattedString()} §e(${entity.type.toShortString()}) §7[ID: ${entity.id}]")
                    if (entity is LivingEntity) {
                        lines.add("§cHP: ${"%.1f".format(entity.health)}/${"%.1f".format(entity.maxHealth)}")
                    }
                    if (entity is Display) {
                        val renderState = entity.renderState()
                        if (renderState != null) {
                            val scale = renderState.transformation().get(1.0f).scale()
                            val scaleStr = " | Scale: [${"%.1f, %.1f, %.1f".format(scale.x(), scale.y(), scale.z())}]"
                            val typeStr = when (entity) {
                                is Display.ItemDisplay -> "Item: ${entity.itemStack.hoverName.toUnformattedString()}"
                                is Display.BlockDisplay -> "Block: ${entity.blockState.block}"
                                is Display.TextDisplay -> "Text: \"${entity.text.toUnformattedString()}\""
                                else -> ""
                            }
                            if (typeStr.isNotEmpty()) {
                                lines.add("§6Display: $typeStr$scaleStr")
                            }
                        }
                    }

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
            }
        }
    }

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder
            .executes { context ->
                executeToggleAll(context)
            }
            .then(
                arg("render", BoolArgumentType.bool())
                    .executes { context ->
                        executeSetAll(context)
                    }
            )
            .then(
                arg("id", IntegerArgumentType.integer())
                    .executes { context ->
                        executeToggleId(context)
                    }
                    .then(
                        arg("render", BoolArgumentType.bool())
                            .executes { context ->
                                executeSetId(context)
                            }
                    )
            )
            .then(
                arg("filter", StringArgumentType.word())
                    .then(
                        arg("render", BoolArgumentType.bool())
                            .executes { context ->
                                executeSetFilter(context)
                            }
                    )
            )
    }

    private fun executeToggleAll(context: CommandContext<FabricClientCommandSource>): Int {
        renderAll = !renderAll
        if (renderAll) {
            targetId = null
            filter = null
        }
        context.source.sendFeedback(
            TextUtils.rfuLiteral(
                "Entity rendering on all alive entities: ${if (renderAll) "§aENABLED" else "§cDISABLED"}",
                TextStyle(TextColor.YELLOW)
            )
        )
        return 1
    }

    private fun executeSetAll(context: CommandContext<FabricClientCommandSource>): Int {
        val render = BoolArgumentType.getBool(context, "render")
        renderAll = render
        if (renderAll) {
            targetId = null
            filter = null
        }
        context.source.sendFeedback(
            TextUtils.rfuLiteral(
                "Entity rendering on all alive entities: ${if (renderAll) "§aENABLED" else "§cDISABLED"}",
                TextStyle(TextColor.YELLOW)
            )
        )
        return 1
    }

    private fun executeToggleId(context: CommandContext<FabricClientCommandSource>): Int {
        val id = IntegerArgumentType.getInteger(context, "id")
        if (targetId == id) {
            targetId = null
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Entity rendering on entity ID $id: §cDISABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        } else {
            targetId = id
            renderAll = false
            filter = null
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Entity rendering on entity ID $id: §aENABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        }
        return 1
    }

    private fun executeSetId(context: CommandContext<FabricClientCommandSource>): Int {
        val id = IntegerArgumentType.getInteger(context, "id")
        val render = BoolArgumentType.getBool(context, "render")
        if (render) {
            targetId = id
            renderAll = false
            filter = null
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Entity rendering on entity ID $id: §aENABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        } else {
            if (targetId == id) {
                targetId = null
            }
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Entity rendering on entity ID $id: §cDISABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        }
        return 1
    }

    private fun executeSetFilter(context: CommandContext<FabricClientCommandSource>): Int {
        val f = StringArgumentType.getString(context, "filter")
        val render = BoolArgumentType.getBool(context, "render")
        if (render) {
            filter = f
            renderAll = true
            targetId = null
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Entity rendering on type matching '$f': §aENABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        } else {
            if (filter == f) {
                filter = null
                renderAll = false
            }
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Entity rendering on type matching '$f': §cDISABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        }
        return 1
    }
}
