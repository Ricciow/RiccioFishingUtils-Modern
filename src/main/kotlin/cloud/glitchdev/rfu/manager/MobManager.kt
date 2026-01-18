package cloud.glitchdev.rfu.manager

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.TickEvents
import cloud.glitchdev.rfu.utils.Tablist.getPlayerNames
import gg.essential.universal.utils.toUnformattedString
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import java.util.HashMap
import java.util.HashSet

@AutoRegister
object MobManager : RegisteredEvent {
    private val sbEntities = HashMap<Int, SkyblockEntity>()
    private val uniqueSbEntities = HashSet<SkyblockEntity>()

    private val entityRegex = """\[Lv\d+\] [^\s]+ (.+) \d+\.?\d*(?:k|M)?\/\d+\.?\d*(?:k|M)?❤""".toRegex()

    override fun register() {
        TickEvents.registerTickEvent(0, 10) { client ->
            val world = client.world!!
            scanForSbEntities(world)
            validateCurrentEntities()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            clearAll()
        }
    }

    private fun scanForSbEntities(world: ClientWorld) {
        world.entities.forEach { entity ->
            if (entity !is ArmorStandEntity) return@forEach
            if (sbEntities.containsKey(entity.id)) return@forEach
            if (!entity.isInvisible || !entity.hasCustomName()) return@forEach

            val name = entity.name.toUnformattedString()
            if (!name.matches(entityRegex)) return@forEach
            val sbName = entityRegex.find(name)?.groupValues?.getOrNull(1) ?: return@forEach

            val foundModel = findModelForNametag(entity, world)

            if (foundModel != null) {
                val existingLink = sbEntities[foundModel.id]
                if (existingLink != null && !existingLink.nameTagEntity.isRemoved) {
                    return@forEach
                }

                val sbEntity = SkyblockEntity(entity, foundModel, sbName)

                sbEntities[entity.id] = sbEntity
                sbEntities[foundModel.id] = sbEntity
                uniqueSbEntities.add(sbEntity)
            }
        }
    }

    private fun validateCurrentEntities() {
        val toRemove = uniqueSbEntities.filter { it.isRemoved() }

        toRemove.forEach { entity ->
            removeEntity(entity)
        }
    }

    private fun findModelForNametag(nametag: ArmorStandEntity, world: ClientWorld): LivingEntity? {
        val searchBox = Box(
            nametag.x - 0.5, nametag.y - 4.0, nametag.z - 0.5,
            nametag.x + 0.5, nametag.y + 0.5, nametag.z + 0.5
        )

        val candidates = world.getOtherEntities(nametag, searchBox) { candidate ->
            if (candidate !is LivingEntity || candidate is ArmorStandEntity) return@getOtherEntities false
            if (candidate is PlayerEntity && getPlayerNames().contains(candidate.name.toUnformattedString())) {
                println(candidate.name.toUnformattedString())
                return@getOtherEntities false
            }
            val existingLink = sbEntities[candidate.id]
            existingLink == null || existingLink.nameTagEntity.isRemoved
        }.toList()

        // Return closest candidate
        return candidates.minByOrNull { candidate ->
            val dx = nametag.x - candidate.x
            val dz = nametag.z - candidate.z
            dx * dx + dz * dz
        } as? LivingEntity
    }

    fun removeEntity(sbEntity: SkyblockEntity) {
        uniqueSbEntities.remove(sbEntity)
        sbEntities.remove(sbEntity.modelEntity.id)
        sbEntities.remove(sbEntity.nameTagEntity.id)
    }

    fun findSbEntities(regex: Regex): List<SkyblockEntity> {
        return uniqueSbEntities.filter { entity ->
            regex.matches(entity.sbName)
        }
    }

    fun clearAll() {
        sbEntities.clear()
        uniqueSbEntities.forEach { it.isRegistered = false }
        uniqueSbEntities.clear()
    }
}