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
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.item.ItemEntity
import java.awt.Color

@RFUFeature
object Normal : Feature, AbstractCommand("normal") {
    override val description: String = "Toggles or sets rendering of info on top of currently alive entities"

    var renderAll = false
        private set
    var targetId: Int? = null
        private set
    var filter: String? = null
        private set
    var range: Double? = null
        private set

    override fun onInitialize() {
        registerRenderEvent { context ->
            if (!DevSettings.devMode) return@registerRenderEvent
            if (!renderAll && targetId == null) return@registerRenderEvent

            val world = mc.level ?: return@registerRenderEvent
            val player = mc.player
            val entities = world.entitiesForRendering().filter { it.isAlive }

            val targetEntities = if (targetId != null) {
                entities.filter { it.id == targetId }
            } else if (renderAll) {
                val f = filter
                val r = range
                entities.filter { entity ->
                    if (r != null && player != null && entity.distanceTo(player) > r) {
                        return@filter false
                    }
                    if (f != null) {
                        entity.type.toShortString().contains(f, ignoreCase = true) ||
                        entity.type.description.toUnformattedString().contains(f, ignoreCase = true)
                    } else {
                        true
                    }
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

                    if (player != null) {
                        val dist = entity.distanceTo(player)
                        lines.add("§7Dist: §f${"%.1f".format(dist)}m §7| Age: §f${entity.tickCount}t (${"%.1f".format(entity.tickCount / 20.0)}s)")
                    }

                    val flags = mutableListOf<String>()
                    if (entity.isInvisible) flags.add("Invisible")
                    if (entity.isCurrentlyGlowing) flags.add("Glowing")
                    if (entity.isSilent) flags.add("Silent")
                    if (entity.isNoGravity) flags.add("NoGravity")
                    if (entity.isInvulnerable) flags.add("Invulnerable")
                    if (flags.isNotEmpty()) {
                        lines.add("§8[§7${flags.joinToString(", ")}§8]")
                    }

                    if (entity is LivingEntity) {
                        lines.add("§cHP: ${"%.1f".format(entity.health)}/${"%.1f".format(entity.maxHealth)}")
                    }

                    if (entity is ArmorStand) {
                        val asFlags = mutableListOf<String>()
                        if (entity.isMarker) asFlags.add("Marker")
                        if (entity.isSmall) asFlags.add("Small")
                        if (!entity.showBasePlate()) asFlags.add("NoBasePlate")
                        if (entity.showArms()) asFlags.add("ShowArms")
                        if (asFlags.isNotEmpty()) {
                            lines.add("§3ArmorStand: [${asFlags.joinToString(", ")}]")
                        }
                    }

                    if (entity is ItemEntity) {
                        lines.add("§eItem: §f${entity.item.hoverName.toUnformattedString()} §7(x${entity.item.count})")
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

                    if (entity.passengers.isNotEmpty()) {
                        val passengerList = entity.passengers.joinToString(", ") {
                            "§f${it.name.toUnformattedString()} §e(${it.type.toShortString()}) §7[ID: ${it.id}]"
                        }
                        lines.add("§dPassengers (${entity.passengers.size}): $passengerList")
                    }

                    val vehicle = entity.vehicle
                    if (vehicle != null) {
                        lines.add("§bRiding: §f${vehicle.name.toUnformattedString()} §e(${vehicle.type.toShortString()}) §7[ID: ${vehicle.id}]")
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
                lit("range")
                    .executes { context ->
                        executeShowRange(context)
                    }
                    .then(
                        lit("clear")
                            .executes { context ->
                                executeClearRange(context)
                            }
                    )
                    .then(
                        arg("range", DoubleArgumentType.doubleArg(0.0))
                            .executes { context ->
                                executeSetRange(context)
                            }
                            .then(
                                arg("render", BoolArgumentType.bool())
                                    .executes { context ->
                                        executeSetRange(context)
                                    }
                            )
                    )
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

    fun executeToggleAll(context: CommandContext<FabricClientCommandSource>): Int {
        renderAll = !renderAll
        if (renderAll) {
            targetId = null
            filter = null
        }
        val rangeMsg = if (range != null) " §7(range: ${range}m)" else ""
        context.source.sendFeedback(
            TextUtils.debugLiteral(
                "Entity rendering on alive entities$rangeMsg: ${if (renderAll) "§aENABLED" else "§cDISABLED"}",
                TextStyle(TextColor.YELLOW)
            )
        )
        return 1
    }

    fun executeSetAll(context: CommandContext<FabricClientCommandSource>): Int {
        val render = BoolArgumentType.getBool(context, "render")
        renderAll = render
        if (renderAll) {
            targetId = null
            filter = null
        }
        val rangeMsg = if (range != null) " §7(range: ${range}m)" else ""
        context.source.sendFeedback(
            TextUtils.debugLiteral(
                "Entity rendering on alive entities$rangeMsg: ${if (renderAll) "§aENABLED" else "§cDISABLED"}",
                TextStyle(TextColor.YELLOW)
            )
        )
        return 1
    }

    fun executeSetRange(context: CommandContext<FabricClientCommandSource>): Int {
        val r = DoubleArgumentType.getDouble(context, "range")
        val render = try {
            BoolArgumentType.getBool(context, "render")
        } catch (e: IllegalArgumentException) {
            true
        }

        if (r <= 0.0) {
            range = null
            renderAll = render
            context.source.sendFeedback(
                TextUtils.debugLiteral(
                    "Entity rendering range limit §cCLEARED §e(rendering on alive entities: ${if (renderAll) "§aENABLED" else "§cDISABLED"}§e)",
                    TextStyle(TextColor.YELLOW)
                )
            )
        } else {
            range = r
            renderAll = render
            targetId = null
            context.source.sendFeedback(
                TextUtils.debugLiteral(
                    "Entity rendering range set to ${r}m: ${if (renderAll) "§aENABLED" else "§cDISABLED"}",
                    TextStyle(TextColor.YELLOW)
                )
            )
        }
        return 1
    }

    fun executeClearRange(context: CommandContext<FabricClientCommandSource>): Int {
        range = null
        context.source.sendFeedback(
            TextUtils.debugLiteral(
                "Entity rendering range limit §cCLEARED §e(rendering: ${if (renderAll) "§aENABLED" else "§cDISABLED"}§e)",
                TextStyle(TextColor.YELLOW)
            )
        )
        return 1
    }

    fun executeShowRange(context: CommandContext<FabricClientCommandSource>): Int {
        val rangeStr = if (range != null) "§a${range}m" else "§cNone (infinite)"
        val statusStr = if (renderAll) "§aENABLED" else "§cDISABLED"
        context.source.sendFeedback(
            TextUtils.debugLiteral(
                "Entity rendering status: $statusStr §e| Range: $rangeStr",
                TextStyle(TextColor.YELLOW)
            )
        )
        return 1
    }

    fun executeToggleId(context: CommandContext<FabricClientCommandSource>): Int {
        val id = IntegerArgumentType.getInteger(context, "id")
        if (targetId == id) {
            targetId = null
            context.source.sendFeedback(
                TextUtils.debugLiteral(
                    "Entity rendering on entity ID $id: §cDISABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        } else {
            targetId = id
            renderAll = false
            filter = null
            context.source.sendFeedback(
                TextUtils.debugLiteral(
                    "Entity rendering on entity ID $id: §aENABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        }
        return 1
    }

    fun executeSetId(context: CommandContext<FabricClientCommandSource>): Int {
        val id = IntegerArgumentType.getInteger(context, "id")
        val render = BoolArgumentType.getBool(context, "render")
        if (render) {
            targetId = id
            renderAll = false
            filter = null
            context.source.sendFeedback(
                TextUtils.debugLiteral(
                    "Entity rendering on entity ID $id: §aENABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        } else {
            if (targetId == id) {
                targetId = null
            }
            context.source.sendFeedback(
                TextUtils.debugLiteral(
                    "Entity rendering on entity ID $id: §cDISABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        }
        return 1
    }

    fun executeSetFilter(context: CommandContext<FabricClientCommandSource>): Int {
        val f = StringArgumentType.getString(context, "filter")
        val render = BoolArgumentType.getBool(context, "render")
        if (render) {
            filter = f
            renderAll = true
            targetId = null
            val rangeMsg = if (range != null) " §7(range: ${range}m)" else ""
            context.source.sendFeedback(
                TextUtils.debugLiteral(
                    "Entity rendering on type matching '$f'$rangeMsg: §aENABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        } else {
            if (filter == f) {
                filter = null
                renderAll = false
            }
            context.source.sendFeedback(
                TextUtils.debugLiteral(
                    "Entity rendering on type matching '$f': §cDISABLED",
                    TextStyle(TextColor.YELLOW)
                )
            )
        }
        return 1
    }
}
