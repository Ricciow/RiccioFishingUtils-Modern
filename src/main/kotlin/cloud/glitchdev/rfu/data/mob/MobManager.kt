package cloud.glitchdev.rfu.data.mob

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.EntityAddedEvents.registerEntityAddedEvent
import cloud.glitchdev.rfu.events.managers.EntityDataEvents.registerEntityDataEvent
import cloud.glitchdev.rfu.events.managers.EntityRemovedEvents.registerEntityRemovedEvent
import cloud.glitchdev.rfu.events.managers.EntityStatusEvents.registerEntityStatusEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.MobEvents
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player

@AutoRegister
object MobManager : RegisteredEvent {
    private val sbEntities = HashMap<Int, SkyblockEntity>()
    private val uniqueSbEntities = HashSet<SkyblockEntity>()
    private val pendingNametags = HashSet<Int>()

    override fun register() {
        registerEntityAddedEvent { entity ->
            val world = entity.level() as? ClientLevel ?: return@registerEntityAddedEvent
            if (entity is ArmorStand && checkSbEntity(entity, world)) {
                MobEvents.MobDetectEventManager.runTasks(uniqueSbEntities.toSet())
                return@registerEntityAddedEvent
            }

            if (entity is LivingEntity) {
                checkPendingNametags(world)
            }
        }

        registerEntityDataEvent { entity ->
            val world = entity.level() as? ClientLevel ?: return@registerEntityDataEvent
            val trackedEntity = sbEntities[entity.id]
            if (trackedEntity != null) {
                if (entity.id == trackedEntity.nameTagEntity.id) {
                    trackedEntity.updateEntityData()
                    MobEvents.MobUpdateEventManager.runTasks(trackedEntity)
                }
            } else {
                if (entity is ArmorStand && checkSbEntity(entity, world)) {
                    MobEvents.MobDetectEventManager.runTasks(uniqueSbEntities.toSet())
                } else if (entity is LivingEntity) {
                    checkPendingNametags(world)
                }
            }
        }

        registerEntityRemovedEvent { entityId ->
            pendingNametags.remove(entityId)
            val sbEntity = sbEntities[entityId] ?: return@registerEntityRemovedEvent
            if (entityId == sbEntity.nameTagEntity.id) {
                sbEntities.remove(entityId)
                val player = mc.player
                if (player != null && player.distanceToSqr(sbEntity.position()) <= 400.0) {
                    removeEntity(sbEntity)
                    MobEvents.MobDisposeEventManager.runTasks(setOf(sbEntity))
                }
            } else if (sbEntity.modelEntities.any { it.id == entityId }) {
                sbEntities.remove(entityId)
                sbEntity.modelEntities.removeIf { it.id == entityId }
                sbEntity.parts.removeIf { it.id == entityId }

                val aliveModel = sbEntity.modelEntities.firstOrNull { it.isAlive && !it.isRemoved }
                if (aliveModel != null) {
                    sbEntity.modelEntity = aliveModel
                    sbEntity.updateParts()
                    MobEvents.MobUpdateEventManager.runTasks(sbEntity)
                } else {
                    removeEntity(sbEntity)
                    MobEvents.MobDisposeEventManager.runTasks(setOf(sbEntity))
                }
            } else {
                sbEntities.remove(entityId)
                sbEntity.parts.removeIf { it.id == entityId }
            }
        }

        registerEntityStatusEvent { entity, status ->
            if (status == EntityEvent.DEATH) {
                val sbEntity = sbEntities[entity.id] ?: return@registerEntityStatusEvent
                if (!sbEntity.isDying && sbEntity.modelEntities.any { it.id == entity.id }) {
                    sbEntity.isDying = true
                    MobEvents.MobDeathEventManager.runTasks(setOf(sbEntity))
                }
            }
        }

        registerTickEvent(interval = 5) {
            val world = mc.level ?: return@registerTickEvent

            checkPendingNametags(world)

            val toDispose = mutableSetOf<SkyblockEntity>()
            for (sbEntity in uniqueSbEntities.toList()) {
                revalidateMob(sbEntity, world, toDispose)
            }
            if (toDispose.isNotEmpty()) {
                toDispose.forEach { removeEntity(it) }
                MobEvents.MobDisposeEventManager.runTasks(toDispose)
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

    fun getEntities(includeDying: Boolean = false): Set<SkyblockEntity> {
        return if (includeDying) uniqueSbEntities.toSet()
        else uniqueSbEntities.filter { !it.isRemoved() }.toSet()
    }

    fun getSkyblockEntity(id: Int): SkyblockEntity? {
        return sbEntities[id]
    }

    private fun isValidModel(candidate: Entity, sbName: String, nametagId: Int): Boolean {
        if (candidate.id == nametagId) return false
        if (!candidate.isAlive || candidate.isRemoved) return false
        if (candidate is LivingEntity && candidate.isDeadOrDying) return false

        if (candidate is Player) {
            val candidateName = candidate.name.toUnformattedString().trim()
            val matchesSbName = candidateName.equals(sbName.trim(), ignoreCase = true) ||
                sbName.contains(candidateName, ignoreCase = true)
            if (!matchesSbName) return false
        } else if (candidate is ArmorStand) {
            if (candidate.hasCustomName() || candidate.isCustomNameVisible) return false
            if (candidate.getItemBySlot(EquipmentSlot.HEAD).isEmpty) return false
        } else if (candidate !is LivingEntity) {
            return false
        }

        val existingLink = sbEntities[candidate.id]
        if (existingLink != null) {
            if (existingLink.isDying) return false
            if (!existingLink.outdatedNametag() && existingLink.nameTagEntity.id != nametagId) {
                return false
            }
        }

        return true
    }

    fun isSerpentineMob(sbName: String): Boolean {
        return sbName.contains("Fire Eel", ignoreCase = true) ||
            sbName.contains("Titanoboa", ignoreCase = true) ||
            sbName.contains("Wiki Tiki", ignoreCase = true) ||
            sbName.contains("Fiery Scuttler", ignoreCase = true)
    }

    private fun checkPendingNametags(world: ClientLevel) {
        if (pendingNametags.isEmpty()) return
        val snapshot = pendingNametags.toList()
        var detectedAny = false
        for (tagId in snapshot) {
            val tagEntity = world.getEntity(tagId) as? ArmorStand
            if (tagEntity == null || tagEntity.isRemoved) {
                pendingNametags.remove(tagId)
                continue
            }
            if (checkSbEntity(tagEntity, world)) {
                pendingNametags.remove(tagId)
                detectedAny = true
            }
        }
        if (detectedAny) {
            MobEvents.MobDetectEventManager.runTasks(uniqueSbEntities.toSet())
        }
    }

    private fun isSerpentinePart(candidate: Entity, sbName: String, nametagId: Int): Boolean {
        if (candidate.id == nametagId) return false
        if (!candidate.isAlive || candidate.isRemoved) return false

        if (candidate is ArmorStand) {
            return !candidate.hasCustomName() &&
                !candidate.isCustomNameVisible &&
                !candidate.getItemBySlot(EquipmentSlot.HEAD).isEmpty
        }

        if (candidate is LivingEntity) {
            return isValidModel(candidate, sbName, nametagId)
        }

        return false
    }

    private fun findSequentialModels(nametag: ArmorStand, sbName: String, world: ClientLevel): List<LivingEntity> {
        val models = mutableListOf<LivingEntity>()
        val isSerpentine = isSerpentineMob(sbName)

        if (isSerpentine) {
            var currentId = nametag.id - 1
            val maxScan = 32
            var scanned = 0

            while (scanned < maxScan) {
                val candidate = world.getEntity(currentId) ?: break
                if (candidate is ArmorStand && (candidate.hasCustomName() || candidate.isCustomNameVisible)) {
                    break
                }
                if (isSerpentinePart(candidate, sbName, nametag.id)) {
                    if (candidate is LivingEntity) {
                        models.add(candidate)
                    }
                    currentId--
                    scanned++
                } else {
                    break
                }
            }
        } else {
            var currentId = nametag.id - 1
            val maxScan = 3
            var scanned = 0

            while (currentId >= nametag.id - maxScan && scanned < maxScan) {
                val candidate = world.getEntity(currentId)
                if (candidate != null) {
                    if (candidate is ArmorStand && (candidate.hasCustomName() || candidate.isCustomNameVisible)) {
                        break
                    }
                    if (candidate is LivingEntity && isValidModel(candidate, sbName, nametag.id)) {
                        models.add(candidate)

                        var currentVehicle = candidate.vehicle
                        while (currentVehicle is LivingEntity && isValidModel(currentVehicle, sbName, nametag.id)) {
                            if (!models.contains(currentVehicle)) {
                                models.add(currentVehicle)
                            }
                            currentVehicle = currentVehicle.vehicle
                        }

                        break
                    }
                }
                currentId--
                scanned++
            }
        }

        return models
    }

    private fun checkSbEntity(entity: ArmorStand, world: ClientLevel): Boolean {
        val trackedEntity = sbEntities[entity.id]
        if (trackedEntity != null && !trackedEntity.outdatedNametag()) return false

        if (!entity.isInvisible) return false

        val parsed = SkyblockEntity.parseNameTag(entity) ?: return false
        if (parsed.health == "0") return false

        val foundModels = findSequentialModels(entity, parsed.sbName, world)
        if (foundModels.isEmpty()) {
            pendingNametags.add(entity.id)
            return false
        }

        val isSerpentine = isSerpentineMob(parsed.sbName)

        val validModels = foundModels.filter { model ->
            val owner = sbEntities[model.id]
            owner == null || owner.outdatedNametag() || owner.nameTagEntity.id == entity.id
        }

        if (validModels.isEmpty()) {
            pendingNametags.add(entity.id)
            return false
        }
        pendingNametags.remove(entity.id)

        val existingLink = validModels.firstNotNullOfOrNull { sbEntities[it.id] }

        if (existingLink != null) {
            if (existingLink.isDying) return false
            if (existingLink.outdatedNametag()) {
                sbEntities.remove(existingLink.nameTagEntity.id)
                existingLink.updateNametag(entity)
                existingLink.updateEntityData()
                sbEntities[entity.id] = existingLink
                for (part in existingLink.parts) {
                    sbEntities[part.id] = existingLink
                }
                MobEvents.MobUpdateEventManager.runTasks(existingLink)
                return false
            } else if (isSerpentine && existingLink.nameTagEntity.id == entity.id) {
                existingLink.modelEntities.addAll(validModels)
                existingLink.updateParts()
                sbEntities[entity.id] = existingLink
                for (part in existingLink.parts) {
                    sbEntities[part.id] = existingLink
                }
                MobEvents.MobUpdateEventManager.runTasks(existingLink)
                return false
            } else {
                return false
            }
        } else {
            val assignedModels = if (isSerpentine) validModels else listOf(validModels.first())
            val sbEntity = SkyblockEntity(entity, assignedModels)
            sbEntities[entity.id] = sbEntity

            for (part in sbEntity.parts) {
                sbEntities[part.id] = sbEntity
            }

            uniqueSbEntities.add(sbEntity)
            return true
        }
    }

    private fun revalidateMob(
        sbEntity: SkyblockEntity,
        world: ClientLevel,
        toDispose: MutableSet<SkyblockEntity>
    ) {
        sbEntity.modelEntities.removeIf { !it.isAlive || it.isRemoved }

        val deadParts = mutableListOf<Int>()
        sbEntity.parts.removeIf { part ->
            val dead = !part.isAlive || part.isRemoved
            if (dead) deadParts.add(part.id)
            dead
        }
        for (partId in deadParts) {
            sbEntities.remove(partId)
        }

        if (sbEntity.modelEntities.isEmpty()) {
            toDispose.add(sbEntity)
            return
        }

        if (!sbEntity.modelEntity.isAlive || sbEntity.modelEntity.isRemoved) {
            sbEntity.modelEntity = sbEntity.modelEntities.first()
        }

        if (sbEntity.isDying) {
            return
        }

        val tagDead = sbEntity.outdatedNametag()
        val player = mc.player
        val isWithin20Blocks = player != null && player.distanceToSqr(sbEntity.position()) <= 400.0

        if (tagDead && isWithin20Blocks) {
            val currentTag = world.getEntity(sbEntity.nameTagEntity.id) as? ArmorStand
            if (currentTag != null && !currentTag.isRemoved && SkyblockEntity.isNameTagEntity(currentTag)) {
                sbEntities.remove(sbEntity.nameTagEntity.id)
                sbEntity.updateNametag(currentTag)
                sbEntity.updateEntityData()
                sbEntities[currentTag.id] = sbEntity
                MobEvents.MobUpdateEventManager.runTasks(sbEntity)
            }
        }

        if (!sbEntity.outdatedNametag()) {
            val oldHealth = sbEntity.health
            sbEntity.updateEntityData()
            if (sbEntity.health != oldHealth) {
                MobEvents.MobUpdateEventManager.runTasks(sbEntity)
            }
        }
    }

    fun removeEntity(sbEntity: SkyblockEntity) {
        sbEntity.dispose()
        uniqueSbEntities.remove(sbEntity)
        sbEntities.remove(sbEntity.nameTagEntity.id)
        for (model in sbEntity.modelEntities) {
            sbEntities.remove(model.id)
        }
        for (part in sbEntity.parts) {
            sbEntities.remove(part.id)
        }
    }

    fun clearAll() {
        pendingNametags.clear()
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