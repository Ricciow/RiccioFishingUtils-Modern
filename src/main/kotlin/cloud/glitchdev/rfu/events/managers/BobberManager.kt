package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.fishing.BobberInfo
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.EntityAddedEvents.registerEntityAddedEvent
import cloud.glitchdev.rfu.events.managers.EntityRemovedEvents.registerEntityRemovedEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.phys.Vec3
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@AutoRegister
object BobberManager : RegisteredEvent {
    private val activeBobbers = ConcurrentHashMap<Int, BobberInfo>()
    private val recentlyRemovedBobbers = ArrayList<BobberInfo>()
    private val entityToBobberMap = ConcurrentHashMap<Int, BobberInfo>()

    override fun register() {
        registerEntityAddedEvent { entity ->
            if (entity is FishingHook) {
                val owner = entity.playerOwner
                val ownerName = owner?.name?.toUnformattedString()
                val ownerUUID = owner?.uuid
                val info = BobberInfo(
                    entityId = entity.id,
                    ownerName = ownerName,
                    ownerUUID = ownerUUID,
                    lastPos = entity.position()
                )
                activeBobbers[entity.id] = info
            } else if (entity is LivingEntity && entity !is ArmorStand) {
                // Pre-link at spawn time (exact coordinates, horizontal match)
                val matched = findClosestBobber(entity.position(), maxDistance = 1.0)
                if (matched != null) {
                    entityToBobberMap[entity.id] = matched
                }
            }
        }

        registerEntityRemovedEvent { entityId ->
            val info = activeBobbers.remove(entityId)
            if (info != null) {
                info.removedTime = Clock.System.now()
                synchronized(recentlyRemovedBobbers) {
                    recentlyRemovedBobbers.add(info)
                }
            }
            entityToBobberMap.remove(entityId)
        }

        registerTickEvent(interval = 1) {
            val world = mc.level ?: return@registerTickEvent

            activeBobbers.forEach { (id, info) ->
                val entity = world.getEntity(id) as? FishingHook ?: return@forEach

                info.lastPos = entity.position()
                if (info.ownerName == null) {
                    val owner = entity.playerOwner
                    if (owner != null) {
                        info.ownerName = owner.name.toUnformattedString()
                        info.ownerUUID = owner.uuid
                    }
                }
            }

            val now = Clock.System.now()
            synchronized(recentlyRemovedBobbers) {
                recentlyRemovedBobbers.removeAll { info ->
                    val removedTime = info.removedTime
                    removedTime != null && (now - removedTime) > 3.seconds
                }
            }
        }

        registerJoinEvent {
            clearAll()
        }

        registerDisconnectEvent {
            clearAll()
        }
    }

    private fun clearAll() {
        activeBobbers.clear()
        synchronized(recentlyRemovedBobbers) {
            recentlyRemovedBobbers.clear()
        }
        entityToBobberMap.clear()
    }

    fun getActiveBobbers(): List<BobberInfo> {
        return activeBobbers.values.toList()
    }

    fun getRecentlyRemovedBobbers(): List<BobberInfo> {
        return synchronized(recentlyRemovedBobbers) {
            ArrayList(recentlyRemovedBobbers)
        }
    }

    fun getBobberForEntity(entityId: Int): BobberInfo? {
        return entityToBobberMap[entityId]
    }

    /**
     * Finds the closest bobber (active or recently removed) to the given position within a certain max distance.
     * Checks horizontal distance with vertical tolerance.
     */
    fun findClosestBobber(pos: Vec3, maxDistance: Double = 0.5): BobberInfo? {
        val candidates = ArrayList<BobberInfo>()
        candidates.addAll(activeBobbers.values)
        synchronized(recentlyRemovedBobbers) {
            candidates.addAll(recentlyRemovedBobbers)
        }

        return candidates
            .filter { 
                val dx = it.lastPos.x - pos.x
                val dz = it.lastPos.z - pos.z
                val horizontalDistSqr = dx * dx + dz * dz
                val dy = Math.abs(it.lastPos.y - pos.y)
                horizontalDistSqr <= maxDistance * maxDistance && dy <= 3.0
            }
            .minByOrNull { 
                val dx = it.lastPos.x - pos.x
                val dz = it.lastPos.z - pos.z
                dx * dx + dz * dz
            }
    }
}
