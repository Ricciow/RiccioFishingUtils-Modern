package cloud.glitchdev.rfu.data.drops

import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDeathEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Coroutines
import cloud.glitchdev.rfu.utils.TextUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CopyOnWriteArrayList

@AutoRegister
object DeadMobTracker : RegisteredEvent {

    data class DeadMob(
        val entityId: Int,
        val name: String,
        val position: Vec3,
        val timestamp: Long = System.currentTimeMillis(),
        var consumed: Boolean = false
    )

    private class PendingLookup(
        val relatedScs: List<SeaCreatures>,
        val playerPos: Vec3?,
        val dropTimestamp: Long,
        val windowMs: Long,
        val onResolved: (String?) -> Unit
    ) {
        var timeoutJob: Job? = null
    }

    private val records = CopyOnWriteArrayList<DeadMob>()
    private val pendingLookups = CopyOnWriteArrayList<PendingLookup>()

    override fun register() {
        registerMobDeathEvent(0) { mobs ->
            recordDeaths(mobs)
        }

        registerLocationEvent {
            clear()
        }

        registerDisconnectEvent {
            clear()
        }
    }

    fun recordDeaths(mobs: Set<SkyblockEntity>) {
        val now = System.currentTimeMillis()
        mobs.forEach { mob ->
            val name = mob.getName() ?: mob.sbName
            val pos = mob.modelEntity.position()
            records.add(
                DeadMob(
                    entityId = mob.modelEntity.id,
                    name = name,
                    position = pos,
                    timestamp = now
                )
            )
        }
        cleanExpired()
        checkPendingLookups()
    }

    private fun checkPendingLookups() {
        if (pendingLookups.isEmpty()) return

        for (lookup in pendingLookups) {
            val mob = findAndConsume(lookup.relatedScs, lookup.playerPos, lookup.dropTimestamp, lookup.windowMs)
            if (mob != null) {
                if (pendingLookups.remove(lookup)) {
                    lookup.timeoutJob?.cancel()
                    lookup.onResolved(mob)
                }
            }
        }
    }

    fun cleanExpired(maxAgeMs: Long = 1000L) {
        val now = System.currentTimeMillis()
        records.removeIf { now - it.timestamp > maxAgeMs }
    }

    fun findOrWait(
        relatedScs: List<SeaCreatures>,
        playerPos: Vec3?,
        dropTimestamp: Long = System.currentTimeMillis(),
        windowMs: Long = 50L,
        timeoutMs: Long = 50L,
        onFound: (String?) -> Unit
    ) {
        val immediateMob = findAndConsume(relatedScs, playerPos, dropTimestamp, windowMs)
        if (immediateMob != null) {
            onFound(immediateMob)
            return
        }

        val lookup = PendingLookup(relatedScs, playerPos, dropTimestamp, windowMs, onFound)

        lookup.timeoutJob = Coroutines.launch {
            delay(timeoutMs)
            if (pendingLookups.remove(lookup)) {
                val finalAttempt = findAndConsume(relatedScs, playerPos, dropTimestamp, windowMs)
                onFound(finalAttempt)
            }
        }

        pendingLookups.add(lookup)
    }

    fun findAndConsume(
        relatedScs: List<SeaCreatures>,
        playerPos: Vec3?,
        dropTimestamp: Long = System.currentTimeMillis(),
        windowMs: Long = 50L
    ): String? {
        cleanExpired()

        val minTime = dropTimestamp - windowMs
        val maxTime = dropTimestamp + windowMs

        var candidates = records.filter { !it.consumed && it.timestamp in minTime..maxTime }

        if (relatedScs.isNotEmpty()) {
            val allowedNames = relatedScs.flatMap { listOf(it.scName, it.scDisplayName) }.toSet()
            candidates = candidates.filter { it.name in allowedNames }
        }

        if (candidates.isEmpty()) return null

        val chosen = if (playerPos != null) {
            candidates.minByOrNull { it.position.distanceToSqr(playerPos) }
        } else {
            candidates.firstOrNull()
        }

        chosen?.consumed = true
        return chosen?.name
    }

    fun tryConsumeByName(
        mobName: String,
        dropTimestamp: Long = System.currentTimeMillis(),
        windowMs: Long = 50L
    ): Boolean {
        cleanExpired()

        val minTime = dropTimestamp - windowMs
        val maxTime = dropTimestamp + windowMs

        val candidate = records.firstOrNull {
            !it.consumed && it.name.equals(mobName, ignoreCase = true) && it.timestamp in minTime..maxTime
        }

        if (candidate != null) {
            candidate.consumed = true
            return true
        }
        return false
    }

    fun clear() {
        records.clear()
        pendingLookups.forEach { lookup ->
            lookup.timeoutJob?.cancel()
            lookup.onResolved(null)
        }
        pendingLookups.clear()
    }
}
