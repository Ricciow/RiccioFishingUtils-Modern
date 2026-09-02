package cloud.glitchdev.rfu.data.mob

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.EntityAddedEvents.registerEntityAddedEvent
import cloud.glitchdev.rfu.events.managers.EntityDataEvents.registerEntityDataEvent
import cloud.glitchdev.rfu.events.managers.EntityRemovedEvents.registerEntityRemovedEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.MobEvents
import cloud.glitchdev.rfu.utils.Tablist.getPlayerNames
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB

@AutoRegister
object MobManager : RegisteredEvent {
    private val sbEntities = HashMap<Int, SkyblockEntity>()
    private val uniqueSbEntities = HashSet<SkyblockEntity>()

    override fun register() {
        registerEntityAddedEvent { entity ->
            if (entity is ArmorStand) {
                val world = entity.level() as? ClientLevel ?: return@registerEntityAddedEvent
                if (checkSbEntity(entity, world)) {
                    MobEvents.MobDetectEventManager.runTasks(uniqueSbEntities.toSet())
                }
            }
        }

        registerEntityDataEvent { entity ->
            if (entity is ArmorStand) {
                val trackedEntity = sbEntities[entity.id]
                if (trackedEntity != null) {
                    trackedEntity.updateEntityData()
                    MobEvents.MobUpdateEventManager.runTasks(trackedEntity)
                } else {
                    val world = entity.level() as? ClientLevel ?: return@registerEntityDataEvent
                    if (checkSbEntity(entity, world)) {
                        MobEvents.MobDetectEventManager.runTasks(uniqueSbEntities.toSet())
                    }
                }
            }
        }

        registerEntityRemovedEvent { entityId ->
            val sbEntity = sbEntities[entityId] ?: return@registerEntityRemovedEvent
            if (entityId == sbEntity.nameTagEntity.id) {
                sbEntities.remove(entityId)
            } else if (entityId == sbEntity.modelEntity.id) {
                removeEntity(sbEntity)
                MobEvents.MobDisposeEventManager.runTasks(setOf(sbEntity))
            }
        }

        registerLocationEvent {
            clearAll()
        }

        registerJoinEvent {
            clearAll()
        }

        registerDisconnectEvent {
            clearAll()
        }
    }

    fun getEntities() : Set<SkyblockEntity> {
        return uniqueSbEntities.toSet()
    }

    fun getSkyblockEntity(id: Int): SkyblockEntity? {
        return sbEntities[id]
    }

    private fun checkSbEntity(entity: ArmorStand, world: ClientLevel): Boolean {
        val trackedEntity = sbEntities[entity.id]
        if (trackedEntity != null && !trackedEntity.nameTagEntity.isRemoved) return false

        if (!entity.isInvisible) return false

        if (!SkyblockEntity.isNameTagEntity(entity)) return false

        val foundModel = findModelForNametag(entity, world)

        if (foundModel != null) {
            val existingLink = sbEntities[foundModel.id]

            if (existingLink != null) {
                if (existingLink.nameTagEntity.isRemoved) {
                    sbEntities.remove(existingLink.nameTagEntity.id)
                    existingLink.updateNametag(entity)
                    sbEntities[entity.id] = existingLink
                    return true
                }
            } else {
                val sbEntity = SkyblockEntity(entity, foundModel)
                sbEntities[entity.id] = sbEntity
                sbEntities[foundModel.id] = sbEntity
                uniqueSbEntities.add(sbEntity)
                return true
            }
        }
        return false
    }

    private fun findModelForNametag(nametag: ArmorStand, world: ClientLevel): LivingEntity? {
        val searchBox = AABB(
            nametag.x - 0.5, nametag.y - 4.0, nametag.z - 0.5,
            nametag.x + 0.5, nametag.y + 0.5, nametag.z + 0.5
        )

        val candidates = world.getEntities(nametag, searchBox) { candidate ->
            if (candidate !is LivingEntity || candidate is ArmorStand) return@getEntities false
            if (candidate is Player && getPlayerNames().contains(candidate.name.toUnformattedString())) return@getEntities false
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
        val oldEntities = uniqueSbEntities.toSet()
        sbEntities.clear()
        uniqueSbEntities.forEach {
            it.dispose()
        }
        uniqueSbEntities.clear()
        if (oldEntities.isNotEmpty()) {
            MobEvents.MobDisposeEventManager.runTasks(oldEntities)
        }
        MobEvents.MobDetectEventManager.runTasks(emptySet())
    }
}