package cloud.glitchdev.rfu.manager.mob

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.MobDetectEvents
import cloud.glitchdev.rfu.events.managers.TickEvents
import cloud.glitchdev.rfu.events.managers.WorldChangeEvents.registerWorldChangeEvent
import cloud.glitchdev.rfu.utils.Tablist.getPlayerNames
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box

@AutoRegister
object MobManager : RegisteredEvent {
    private val sbEntities = HashMap<Int, SkyblockEntity>()
    private val uniqueSbEntities = HashSet<SkyblockEntity>()

    override fun register() {
        TickEvents.registerTickEvent(0, 10) { client ->
            val world = client.world ?: return@registerTickEvent
            scanEntities(world)
            validateCurrentEntities()
            MobDetectEvents.runTasks(uniqueSbEntities.toSet())
        }

        registerWorldChangeEvent {
            clearAll()
        }
    }

    private fun scanEntities(world: ClientWorld) {
        world.entities.forEach { entity ->
            if (entity !is ArmorStandEntity) return@forEach
            
            DeployableManager.checkEntity(entity)
            checkSbEntity(entity, world)
        }
    }

    private fun checkSbEntity(entity: ArmorStandEntity, world: ClientWorld) {
        val trackedEntity = sbEntities[entity.id]
        if (trackedEntity != null && !trackedEntity.nameTagEntity.isRemoved) return

        if (!entity.isInvisible) return

        val sbName = SkyblockEntity.parseNameFromTag(entity) ?: return

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
    }
}