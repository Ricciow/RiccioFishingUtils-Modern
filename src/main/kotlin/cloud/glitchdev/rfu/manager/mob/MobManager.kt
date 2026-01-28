package cloud.glitchdev.rfu.manager.mob

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.MobDetectEvents
import cloud.glitchdev.rfu.events.managers.TickEvents
import cloud.glitchdev.rfu.events.managers.WorldChangeEvents.registerWorldChangeEvent
import cloud.glitchdev.rfu.utils.Tablist.getPlayerNames
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.world.ClientWorld
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.PlayerHeadItem
import net.minecraft.util.math.Box

@AutoRegister
object MobManager : RegisteredEvent {
    private val sbEntities = HashMap<Int, SkyblockEntity>()
    private val uniqueSbEntities = HashSet<SkyblockEntity>()
    private val seenFlares = HashSet<Int>()
    var activeFlareEndTime: Long? = null
        private set
    var activeFlareType: FlareType = FlareType.NONE
        private set

    enum class FlareType(val bonus: String, val texture: String) {
        SOS("+125%", "ewogICJ0aW1lc3RhbXAiIDogMTY2MjY4Mjc3NjUxNiwKICAicHJvZmlsZUlkIiA6ICI4YjgyM2E1YmU0Njk0YjhiOTE0NmE5MWRhMjk4ZTViNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXBoaXRpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMDA2MmNjOThlYmRhNzJhNmE0Yjg5NzgzYWRjZWYyODE1YjQ4M2EwMWQ3M2VhODdiM2RmNzYwNzJhODlkMTNiIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="),
        ALERT("+50%", "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1MDQzMTY4MywKICAicHJvZmlsZUlkIiA6ICJmODg2ZDI3YjhjNzU0NjAyODYyYTM1M2NlYmYwZTgwZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb2JpbkdaIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlkMmJmOTg2NDcyMGQ4N2ZkMDZiODRlZmE4MGI3OTVjNDhlZDUzOWIxNjUyM2MzYjFmMTk5MGI0MGMwMDNmNmIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="),
        NONE("", "")
    }

    override fun register() {
        TickEvents.registerTickEvent(0, 10) { client ->
            val world = client.world ?: return@registerTickEvent
            scanForSbEntities(world)
            scanForDeployables(world)
            validateCurrentEntities()
            MobDetectEvents.runTasks(uniqueSbEntities.toSet())
        }

        registerWorldChangeEvent {
            clearAll()
        }
    }

    private fun scanForSbEntities(world: ClientWorld) {
        world.entities.forEach { entity ->
            if (entity !is ArmorStandEntity) return@forEach

            val trackedEntity = sbEntities[entity.id]
            if (trackedEntity != null && !trackedEntity.nameTagEntity.isRemoved) return@forEach

            if (!entity.isInvisible) return@forEach

            val sbName = SkyblockEntity.parseNameFromTag(entity) ?: return@forEach

            val foundModel = findModelForNametag(entity, world)

            if (foundModel != null) {
                val existingLink = sbEntities[foundModel.id]

                if (existingLink != null) {
                    if (existingLink.nameTagEntity.isRemoved) {
                        sbEntities.remove(existingLink.nameTagEntity.id)
                        existingLink.updateNametag(entity)
                        sbEntities[entity.id] = existingLink
                    }
                } else {
                    val sbEntity = SkyblockEntity(entity, foundModel, sbName)
                    sbEntities[entity.id] = sbEntity
                    sbEntities[foundModel.id] = sbEntity
                    uniqueSbEntities.add(sbEntity)
                }
            }
        }
    }

    private fun scanForDeployables(world: ClientWorld) {
        world.entities.forEach { entity ->
            if (entity !is ArmorStandEntity) return@forEach
            if (seenFlares.contains(entity.id)) return@forEach

            val helmet = entity.getEquippedStack(EquipmentSlot.HEAD)

            if (helmet.item !is PlayerHeadItem) return@forEach

            val component = helmet.get(DataComponentTypes.PROFILE)

            if (component != null) {
                val textures = component.gameProfile.properties.get("textures").map { it.value }
                val type = FlareType.entries.find { type -> textures.contains(type.texture) }
                if (type != null && type != FlareType.NONE) {
                    seenFlares.add(entity.id)
                    activeFlareEndTime = System.currentTimeMillis() + 180_000 // 3 minutes
                    activeFlareType = type
                }
            }
        }
    }

    private fun validateCurrentEntities() {
        val toRemove = uniqueSbEntities.filter { it.isRemoved() }
        toRemove.forEach { removeEntity(it) }
    }

    private fun findModelForNametag(nametag: ArmorStandEntity, world: ClientWorld): LivingEntity? {
        val searchBox = Box(
            nametag.x - 0.5, nametag.y - 4.0, nametag.z - 0.5,
            nametag.x + 0.5, nametag.y + 0.5, nametag.z + 0.5
        )

        val candidates = world.getOtherEntities(nametag, searchBox) { candidate ->
            if (candidate !is LivingEntity || candidate is ArmorStandEntity) return@getOtherEntities false
            if (candidate is PlayerEntity && getPlayerNames().contains(candidate.name.toUnformattedString())) return@getOtherEntities false
            val existingLink = sbEntities[candidate.id]
            existingLink == null || existingLink.nameTagEntity.isRemoved
        }.toList()

        return candidates.minByOrNull { candidate ->
            val dx = nametag.x - candidate.x
            val dz = nametag.z - candidate.z
            dx * dx + dz * dz
        } as? LivingEntity
    }

    fun removeEntity(sbEntity: SkyblockEntity) {
        sbEntity.dispose()
        uniqueSbEntities.remove(sbEntity)
        sbEntities.remove(sbEntity.modelEntity.id)
        sbEntities.remove(sbEntity.nameTagEntity.id)
    }

    fun clearAll() {
        sbEntities.clear()
        uniqueSbEntities.forEach { it.renderEvent = null }
        uniqueSbEntities.clear()
        resetFlare()
    }

    fun resetFlare() {
        seenFlares.clear()
        activeFlareEndTime = null
        activeFlareType = FlareType.NONE
    }
}