package cloud.glitchdev.rfu.manager.mob

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.MobDetectEvents
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.TickEvents
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
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
    private lateinit var detectionEvent : TickEvents.TickEvent

    override fun register() {
        detectionEvent = registerTickEvent(0, 10) { client ->
            val world = client.level ?: return@registerTickEvent
            scanEntities(world)
            reverifyModels(world)
            validateCurrentEntities()
            MobDetectEvents.runTasks(uniqueSbEntities.toSet())
        }

        registerJoinEvent {
            clearAll()
        }
    }

    fun boostDetectionRate(state : Boolean) {
        if(::detectionEvent.isInitialized) detectionEvent.interval = if(state) 2 else 10
    }

    fun getEntities() : Set<SkyblockEntity> {
        return uniqueSbEntities.toSet()
    }

    private fun scanEntities(world: ClientLevel) {
        var foundAnyFlare = false
        world.entitiesForRendering().forEach { entity ->
            if (entity !is ArmorStand) return@forEach

            if (DeployableManager.checkEntity(entity)) {
                foundAnyFlare = true
            }
            checkSbEntity(entity, world)
        }

        if (!foundAnyFlare) {
            DeployableManager.resetFlare()
        }
    }

    private fun checkSbEntity(entity: ArmorStand, world: ClientLevel) {
        val trackedEntity = sbEntities[entity.id]
        if (trackedEntity != null && !trackedEntity.nameTagEntity.isRemoved) return

        if (!entity.isInvisible) return

        if (!SkyblockEntity.isNameTagEntity(entity)) return

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
                val sbEntity = SkyblockEntity(entity, foundModel)
                sbEntities[entity.id] = sbEntity
                sbEntities[foundModel.id] = sbEntity
                uniqueSbEntities.add(sbEntity)
            }
        }
    }

    private fun validateCurrentEntities() {
        val toRemove = uniqueSbEntities.filter {
            it.updateEntityData()
            it.isRemoved()
        }
        toRemove.forEach { removeEntity(it) }
    }

    private fun reverifyModels(world: ClientLevel) {
        uniqueSbEntities.forEach { sbEntity ->
            val nametag = sbEntity.nameTagEntity
            if (nametag.isRemoved) return@forEach

            val searchBox = AABB(
                nametag.x - 0.5, nametag.y - 4.0, nametag.z - 0.5,
                nametag.x + 0.5, nametag.y + 0.5, nametag.z + 0.5
            )

            val candidates = world.getEntities(nametag, searchBox) { candidate ->
                if (candidate !is LivingEntity || candidate is ArmorStand) return@getEntities false
                if (candidate is Player && getPlayerNames().contains(candidate.name.toUnformattedString())) return@getEntities false

                val existingLink = sbEntities[candidate.id]
                existingLink == null || existingLink.nameTagEntity.isRemoved || existingLink == sbEntity
            }.toList()

            val bestModel = candidates.minByOrNull { candidate ->
                val dx = nametag.x - candidate.x
                val dz = nametag.z - candidate.z
                dx * dx + dz * dz
            } as? LivingEntity

            if (bestModel != null && bestModel.id != sbEntity.modelEntity.id) {
                sbEntities.remove(sbEntity.modelEntity.id)
                sbEntity.modelEntity = bestModel
                sbEntities[bestModel.id] = sbEntity
            }
        }
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
        sbEntities.clear()
        uniqueSbEntities.forEach { it.dispose() }
        uniqueSbEntities.clear()
    }
}