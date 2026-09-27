package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.data.mob.MobManager
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.EntityInteractEvents.registerEntityInteractEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.text
import com.mojang.brigadier.context.CommandContext
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import java.awt.Color

@RFUFeature
object Sb : SimpleCommand("sb"), Feature {
    override val description: String = "Debug command to inspect and render Skyblock entities"

    var enabled = false
    private var lastClickTime = 0L
    private var lastClickedEntityId = -1

    private val clickedSbEntities = mutableSetOf<SkyblockEntity>()
    private val clickedEntityIds = mutableSetOf<Int>()

    override fun onInitialize() {
        registerRenderEvent { context ->
            if (!DevSettings.devMode || !enabled) return@registerRenderEvent

            val entities = MobManager.getEntities()
            if (entities.isEmpty()) return@registerRenderEvent

            Render3D.draw(context) {
                val tickDelta = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
                for (sbEntity in entities) {
                    if (sbEntity.isRemoved()) continue
                    if (!shouldRender(sbEntity)) continue

                    val entityPos = sbEntity.modelEntity.getPosition(tickDelta)
                    val baseLoc = entityPos.add(0.0, sbEntity.modelEntity.bbHeight.toDouble() + 0.5, 0.0)

                    val lines = mutableListOf<String>()
                    val scName = sbEntity.getName() ?: "Unknown"
                    lines.add("${TextColor.LIGHT_GREEN}${TextEffects.BOLD}$scName")

                    val shurikenStr = if (sbEntity.isShurikened) " ${TextColor.AQUAMARINE}[Shurikened]" else ""
                    lines.add("${TextColor.LIGHT_RED}HP: ${TextColor.WHITE}${sbEntity.health}/${sbEntity.maxHealth}$shurikenStr")

                    lines.add("${TextColor.YELLOW}Type: ${TextColor.WHITE}${sbEntity.modelEntity.type.toShortString()}")
                    val modelInfo = if (sbEntity.modelEntities.size > 1) {
                        "${TextColor.GRAY}Primary Model: ${TextColor.WHITE}${sbEntity.modelEntity.id} ${TextColor.YELLOW}(${sbEntity.modelEntities.size} models) ${TextColor.GRAY}| Tag ID: ${TextColor.WHITE}${sbEntity.nameTagEntity.id}"
                    } else {
                        "${TextColor.GRAY}Model ID: ${TextColor.WHITE}${sbEntity.modelEntity.id} ${TextColor.GRAY}| Tag ID: ${TextColor.WHITE}${sbEntity.nameTagEntity.id}"
                    }
                    lines.add(modelInfo)
                    if (sbEntity.modelEntities.size > 1) {
                        lines.add("${TextColor.YELLOW}Models: ${TextColor.WHITE}${sbEntity.modelEntities.joinToString(", ") { "${it.type.toShortString()}[${it.id}]" }}")
                    }

                    val player = mc.player
                    if (player != null) {
                        val dist = sbEntity.modelEntity.distanceTo(player)
                        lines.add("${TextColor.GRAY}Dist: ${TextColor.WHITE}${"%.1f".format(dist)}m ${TextColor.GRAY}| Age: ${TextColor.WHITE}${sbEntity.modelEntity.tickCount}t")
                    }

                    val origin = sbEntity.originBobber
                    if (origin != null) {
                        lines.add("${TextColor.LIGHT_BLUE}Bobber: ${TextColor.WHITE}${origin.entityId} ${TextColor.GRAY}(${origin.ownerName ?: "Unknown"})")
                    }

                    if (sbEntity.parts.size > 1) {
                        lines.add("${TextColor.MAGENTA}Parts (${sbEntity.parts.size}):")
                        for (part in sbEntity.parts) {
                            if (part.id != sbEntity.modelEntity.id) {
                                val desc = if (part is ArmorStand && !part.getItemBySlot(EquipmentSlot.HEAD).isEmpty) "SkullStand" else part.type.toShortString()
                                lines.add("  ${TextColor.GRAY}- ${TextColor.WHITE}$desc ${TextColor.GRAY}[ID: ${part.id}]")
                            }
                        }
                    }

                    val vehicle = sbEntity.modelEntity.vehicle
                    if (vehicle != null) {
                        lines.add("${TextColor.AQUAMARINE}Riding: ${TextColor.WHITE}${vehicle.name.toUnformattedString()} ${TextColor.YELLOW}(${vehicle.type.toShortString()}) ${TextColor.GRAY}[ID: ${vehicle.id}]")
                    }

                    text {
                        position = baseLoc
                        text = lines.joinToString("\n")
                        color = Color.WHITE
                        scale = 0.025f
                        seeThrough = true
                        backgroundOpacity = 0.4f
                    }
                }
            }
        }

        registerEntityInteractEvent { entity ->
            if (!DevSettings.devMode || !enabled) return@registerEntityInteractEvent
            handleEntityRightClick(entity)
        }

        registerLocationEvent {
            clickedSbEntities.clear()
            clickedEntityIds.clear()
        }

        registerDisconnectEvent {
            clickedSbEntities.clear()
            clickedEntityIds.clear()
        }
    }

    private fun shouldRender(sbEntity: SkyblockEntity): Boolean {
        if (clickedSbEntities.contains(sbEntity) ||
            clickedEntityIds.contains(sbEntity.nameTagEntity.id) ||
            sbEntity.parts.any { clickedEntityIds.contains(it.id) }
        ) {
            return true
        }

        val bobber = sbEntity.originBobber
        if (bobber == null || (bobber.ownerName == null && bobber.ownerUUID == null)) {
            return true
        }

        val ownerName = bobber.ownerName
        if (ownerName != null && User.isUser(ownerName)) {
            return true
        }

        val ownerUUID = bobber.ownerUUID
        if (ownerUUID != null && mc.player?.uuid == ownerUUID) {
            return true
        }

        return false
    }

    override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
        enabled = !enabled

        context.source.sendFeedback(
            TextUtils.debugLiteral(
                "Skyblock Entity Inspection & Rendering: ${if (enabled) "${TextColor.LIGHT_GREEN}ENABLED" else "${TextColor.LIGHT_RED}DISABLED"}",
                TextStyle(TextColor.YELLOW)
            )
        )

        return 1
    }

    private fun handleEntityRightClick(entity: Entity) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime < 250 && lastClickedEntityId == entity.id) return
        lastClickTime = now
        lastClickedEntityId = entity.id

        clickedEntityIds.add(entity.id)

        val sbEntity = MobManager.getSkyblockEntity(entity.id)

        if (sbEntity != null) {
            clickedSbEntities.add(sbEntity)
            logEntityInfo(sbEntity, clickedEntity = entity)
        } else {
            val entityName = entity.name.toUnformattedString()
            val lines = mutableListOf<String>()
            lines.add("${TextColor.YELLOW}[RFU MobDebug] ${TextColor.LIGHT_RED}Not tracked as SkyblockEntity")
            lines.add("  ${TextColor.GRAY}Name: ${TextColor.WHITE}$entityName")
            lines.add("  ${TextColor.GRAY}Type: ${TextColor.WHITE}${entity.type.toShortString()} ${TextColor.GRAY}[ID: ${TextColor.YELLOW}${entity.id}${TextColor.GRAY}]")
            if (entity is LivingEntity) {
                lines.add("  ${TextColor.GRAY}HP: ${TextColor.LIGHT_RED}${entity.health}/${entity.maxHealth}")
            }
            Chat.sendMessage(Component.literal(lines.joinToString("\n")))
        }
    }

    fun logEntityInfo(sbEntity: SkyblockEntity, clickedEntity: Entity? = null) {
        val scName = sbEntity.getName() ?: "Unknown"
        val tagId = sbEntity.nameTagEntity.id
        val modelId = sbEntity.modelEntity.id
        val delta = modelId - tagId
        val shurikenStr = if (sbEntity.isShurikened) " ${TextColor.AQUAMARINE}[Shurikened]" else ""

        val lines = mutableListOf<String>()
        lines.add("${TextColor.YELLOW}[RFU MobDebug] ${TextColor.LIGHT_GREEN}${TextEffects.BOLD}$scName ${TextColor.GRAY}(HP: ${TextColor.LIGHT_RED}${sbEntity.health}/${sbEntity.maxHealth}$shurikenStr${TextColor.GRAY})")

        if (clickedEntity != null && clickedEntity.id != modelId && clickedEntity.id != tagId) {
            val clickedDelta = clickedEntity.id - tagId
            lines.add("  ${TextColor.GOLD}Clicked Target: ${TextColor.WHITE}${clickedEntity.type.toShortString()} ${TextColor.GRAY}[ID: ${TextColor.YELLOW}${clickedEntity.id}${TextColor.GRAY}, ${TextColor.AQUAMARINE}Δ: $clickedDelta${TextColor.GRAY}]")
        }

        lines.add("  ${TextColor.GRAY}Tag: ${TextColor.WHITE}ArmorStand ${TextColor.GRAY}[ID: ${TextColor.YELLOW}$tagId${TextColor.GRAY}]")
        lines.add("  ${TextColor.GRAY}Primary Model: ${TextColor.WHITE}${sbEntity.modelEntity.type.toShortString()} ${TextColor.GRAY}[ID: ${TextColor.YELLOW}$modelId${TextColor.GRAY}, ${TextColor.AQUAMARINE}Δ: $delta${TextColor.GRAY}]")

        if (sbEntity.modelEntities.size > 1) {
            val modelsStr = sbEntity.modelEntities.joinToString(", ") {
                "${it.type.toShortString()}[ID:${it.id}, Δ:${it.id - tagId}]"
            }
            lines.add("  ${TextColor.YELLOW}Models (${sbEntity.modelEntities.size}): ${TextColor.WHITE}$modelsStr")
        }

        val vehicle = sbEntity.modelEntity.vehicle
        if (vehicle != null) {
            val vehDelta = vehicle.id - tagId
            lines.add("  ${TextColor.AQUAMARINE}Riding: ${TextColor.WHITE}${vehicle.type.toShortString()} ${TextColor.GRAY}[ID: ${TextColor.YELLOW}${vehicle.id}${TextColor.GRAY}, ${TextColor.AQUAMARINE}Δ: $vehDelta${TextColor.GRAY}]")
        }

        if (sbEntity.parts.size > 1) {
            val partsStr = sbEntity.parts.joinToString(", ") {
                val partDelta = it.id - tagId
                val desc = if (it is ArmorStand && !it.getItemBySlot(EquipmentSlot.HEAD).isEmpty) "SkullStand" else it.type.toShortString()
                "$desc[ID:${it.id}, Δ:$partDelta]"
            }
            lines.add("  ${TextColor.MAGENTA}Parts (${sbEntity.parts.size}): ${TextColor.WHITE}$partsStr")
        }

        val origin = sbEntity.originBobber
        if (origin != null) {
            lines.add("  ${TextColor.LIGHT_BLUE}Origin Bobber: ${TextColor.WHITE}${origin.entityId} ${TextColor.GRAY}(Owner: ${origin.ownerName ?: "Unknown"})")
        }

        Chat.sendMessage(Component.literal(lines.joinToString("\n")))
    }
}
