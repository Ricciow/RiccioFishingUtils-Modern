package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Display

object Alive : AbstractCommand("alive") {
    override val description: String = "Sends a message with all currently alive entities in the world, or details about a specific entity by ID"

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder
            .executes { context ->
                executeList(context)
            }
            .then(
                arg("id", IntegerArgumentType.integer())
                    .executes { context ->
                        executeDetails(context)
                    }
            )
    }

    private fun executeList(context: CommandContext<FabricClientCommandSource>): Int {
        if (!DevSettings.devMode) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Must have developer mode on to use this feature!",
                    TextStyle(TextColor.RED, TextEffects.BOLD)
                )
            )
            return 1
        }

        val world = mc.level
        if (world == null) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "World is not loaded!",
                    TextStyle(TextColor.RED, TextEffects.BOLD)
                )
            )
            return 1
        }

        val aliveEntities = world.entitiesForRendering().filter { it.isAlive }

        if (aliveEntities.isEmpty()) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral("No alive entities found in the world.", TextColor.YELLOW)
            )
            return 1
        }

        val player = mc.player
        val sortedEntities = if (player != null) {
            aliveEntities.sortedByDescending { player.distanceTo(it) }
        } else {
            aliveEntities
        }

        val text = sortedEntities.map { entity ->
            val entityName = entity.name.toUnformattedString()
            val entityType = entity.type.toShortString()
            val positionStr = "X: %.1f, Y: %.1f, Z: %.1f".format(entity.x, entity.y, entity.z)
            val distanceStr = if (player != null) {
                " §d[${"%.1f".format(player.distanceTo(entity))}m]"
            } else {
                ""
            }

            val displayInfo = if (entity is Display) {
                val scale = entity.renderState()?.transformation?.get(1.0f)?.scale()
                val scaleStr = if (scale != null) "Scale: [${"%.1f, %.1f, %.1f".format(scale.x(), scale.y(), scale.z())}]" else ""
                
                val typeStr = when (entity) {
                    is Display.ItemDisplay -> "Item: ${entity.itemStack.hoverName.toUnformattedString()}"
                    is Display.BlockDisplay -> "Block: ${entity.blockState.block}"
                    is Display.TextDisplay -> "Text: \"${entity.text.toUnformattedString()}\""
                    else -> ""
                }
                "\nDisplay: $typeStr $scaleStr"
            } else {
                ""
            }

            Component.literal("\n§7- §a$entityName §e($entityType) §7(ID: ${entity.id})$distanceStr §f[$positionStr]")
                .withStyle(
                    Style.EMPTY
                        .withHoverEvent(
                            HoverEvent.ShowText(
                                Component.literal(
                                    "Type: ${entity.type.description.toUnformattedString()}\n" +
                                    "UUID: ${entity.uuid}\n" +
                                    "Position: ${entity.x}, ${entity.y}, ${entity.z}\n" +
                                    (if (player != null) "Distance: ${"%.3f".format(player.distanceTo(entity))} blocks\n" else "") +
                                    "Size: Width: ${"%.3f".format(entity.bbWidth)}, Height: ${"%.3f".format(entity.bbHeight)}\n" +
                                    "Is Alive: ${entity.isAlive}$displayInfo"
                                )
                            )
                        )
                )
        }.fold(TextUtils.rfuLiteral("Found ${aliveEntities.size} alive entities:")) { acc, component ->
            acc.append(component)
        }

        context.source.sendFeedback(text)

        return 1
    }

    private fun executeDetails(context: CommandContext<FabricClientCommandSource>): Int {
        if (!DevSettings.devMode) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral(
                    "Must have developer mode on to use this feature!",
                    TextStyle(TextColor.RED, TextEffects.BOLD)
                )
            )
            return 1
        }

        val world = mc.level
        if (world == null) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral("World is not loaded!", TextStyle(TextColor.RED, TextEffects.BOLD))
            )
            return 1
        }

        val targetId = IntegerArgumentType.getInteger(context, "id")
        val entity = world.entitiesForRendering().find { it.id == targetId }

        if (entity == null) {
            context.source.sendFeedback(
                TextUtils.rfuLiteral("Entity with ID $targetId not found or not rendered/loaded.", TextColor.RED)
            )
            return 1
        }

        val player = mc.player
        val details = TextUtils.rfuLiteral("Detailed info for entity ID $targetId:\n", TextColor.AQUAMARINE)
            .append("§7- §fType: §e${entity.type.description.toUnformattedString()} (${entity.type.toShortString()})\n")
            .append("§7- §fDisplay Name: §a${entity.displayName.toUnformattedString()}\n")
            .append("§7- §fCustom Name: §a${entity.customName?.toUnformattedString() ?: "None"}\n")
            .append("§7- §fUUID: §d${entity.uuid}\n")
            .append("§7- §fPosition: §bX: ${"%.3f".format(entity.x)}, Y: ${"%.3f".format(entity.y)}, Z: ${"%.3f".format(entity.z)}\n")
            .append("§7- §fVelocity: §bX: ${"%.3f".format(entity.deltaMovement.x)}, Y: ${"%.3f".format(entity.deltaMovement.y)}, Z: ${"%.3f".format(entity.deltaMovement.z)}\n")
            .append("§7- §fSize: §eWidth: ${"%.3f".format(entity.bbWidth)}, Height: ${"%.3f".format(entity.bbHeight)}\n")
            .append("§7- §fBounding Box: §e[${"%.2f".format(entity.boundingBox.minX)}, ${"%.2f".format(entity.boundingBox.minY)}, ${"%.2f".format(entity.boundingBox.minZ)}] -> [${"%.2f".format(entity.boundingBox.maxX)}, ${"%.2f".format(entity.boundingBox.maxY)}, ${"%.2f".format(entity.boundingBox.maxZ)}]\n")
            .append("§7- §fIs Alive: §2${entity.isAlive}\n")
            .append("§7- §fOn Ground: §7${entity.onGround()}\n")

        if (player != null) {
            details.append("§7- §fDistance to Player: §d${"%.3f".format(player.distanceTo(entity))} blocks\n")
        }

        if (entity is Display) {
            val renderState = entity.renderState()
            if (renderState != null) {
                val scale = renderState.transformation().get(1.0f).scale()
                val translation = renderState.transformation().get(1.0f).translation()
                details.append("§7- §fDisplay Scale: §eX: ${"%.3f".format(scale.x())}, Y: ${"%.3f".format(scale.y())}, Z: ${"%.3f".format(scale.z())}\n")
                details.append("§7- §fDisplay Translation: §eX: ${"%.3f".format(translation.x())}, Y: ${"%.3f".format(translation.y())}, Z: ${"%.3f".format(translation.z())}\n")
            }
            details.append("§7- §fView Range: §7${entity.viewRange}\n")

            if (entity is Display.ItemDisplay) {
                val item = entity.itemStack
                details.append("§7- §fDisplay Item: §a${item.hoverName.toUnformattedString()} §7(${item.count}x)\n")
            } else if (entity is Display.BlockDisplay) {
                val block = entity.blockState
                details.append("§7- §fDisplay Block: §a${block.block}\n")
            } else if (entity is Display.TextDisplay) {
                val textComp = entity.text
                details.append("§7- §fDisplay Text: §a\"${textComp.toUnformattedString()}\"\n")
            }
        }

        if (entity is LivingEntity) {
            details.append("§7- §fHealth: §c${"%.1f".format(entity.health)} / ${"%.1f".format(entity.maxHealth)}\n")
        }

        if (entity.vehicle != null) {
            details.append("§7- §fVehicle: §e${entity.vehicle?.type?.toShortString()} §7(ID: ${entity.vehicle?.id})\n")
        }

        if (entity.passengers.isNotEmpty()) {
            val passengersStr = entity.passengers.joinToString { "${it.type.toShortString()} (ID: ${it.id})" }
            details.append("§7- §fPassengers: §e$passengersStr\n")
        }

        details.append("§7- §fMisc Status: §7Glowing: ${entity.isCurrentlyGlowing}, Invisible: ${entity.isInvisible}, Silent: ${entity.isSilent}")

        context.source.sendFeedback(details)
        return 1
    }
}
