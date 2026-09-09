package cloud.glitchdev.rfu.data.mob.strategies

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.mob.DeployableManager.Deployable
import cloud.glitchdev.rfu.data.mob.DeployableType
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import net.minecraft.world.item.PlayerHeadItem
import net.minecraft.world.phys.Vec3
import kotlin.math.round

class FlareStrategy : DeployableStrategy {
    override val type = DeployableType.FLARE

    private enum class FlareType(val priority: Int, val accentLabel: String, val texture: String) {
        SOS(3, "+125%", "ewogICJ0aW1lc3RhbXAiIDogMTY2MjY4Mjc3NjUxNiwKICAicHJvZmlsZUlkIiA6ICI4YjgyM2E1YmU0Njk0YjhiOTE0NmE5MWRhMjk4ZTViNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXBoaXRpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMDA2MmNjOThlYmRhNzJhNmE0Yjg5NzgzYWRjZWYyODE1YjQ4M2EwMWQ3M2VhODdiM2RmNzYwNzJhODlkMTNiIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="),
        ALERT(2, "+50%", "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1MDQzMTY4MywKICAicHJvZmlsZUlkIiA6ICJmODg2ZDI3YjhjNzU0NjAyODYyYTM1M2NlYmYwZTgwZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb2JpbkdaIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlkMmJmOTg2NDcyMGQ4N2ZkMDZiODRlZmE4MGI3OTVjNDhlZDUzOWIxNjUyM2MzYjFmMTk5MGI0MGMwMDNmNmIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="),
        UNDEFINED(1, "", ""),
    }

    private data class TrackedFlare(
        val entityId: Int,
        val flareType: FlareType,
        val endTimeMillis: Long,
        var posX: Double,
        var posZ: Double,
        var highestY: Double,
    ) {
        fun toDeployable(): Deployable {
            return Deployable(
                type = DeployableType.FLARE,
                endTimeMillis = endTimeMillis,
                accentLabel = flareType.accentLabel,
                posX = posX,
                posZ = posZ,
                highestY = highestY,
            )
        }

        fun isInRange(playerPos: Vec3): Boolean {
            val dy = playerPos.y - (round(highestY * 2.0) / 2.0 + 0.35)
            val dx = playerPos.x - posX
            val dz = playerPos.z - posZ
            val r = DeployableType.FLARE.range
            return dx * dx + dy * dy + dz * dz <= r * r
        }

        fun distanceSquared(playerPos: Vec3?): Double {
            if (playerPos == null) return 0.0
            val dy = playerPos.y - (round(highestY * 2.0) / 2.0 + 0.35)
            val dx = playerPos.x - posX
            val dz = playerPos.z - posZ
            return dx * dx + dy * dy + dz * dz
        }
    }

    private data class TrackedFirework(
        val entityId: Int,
        val endTimeMillis: Long,
        var posX: Double,
        var posZ: Double,
        var highestY: Double,
        var lastSeenMillis: Long,
        var claimed: Boolean = false,
    )

    private val activeFlares = HashMap<Int, TrackedFlare>()
    private val recentFireworks = HashMap<Int, TrackedFirework>()
    private val seenThisTick = HashSet<Int>()

    override fun resetSession() {
        activeFlares.clear()
        recentFireworks.clear()
        seenThisTick.clear()
    }

    override fun startTick() {
        seenThisTick.clear()
        val now = System.currentTimeMillis()
        recentFireworks.entries.removeIf { (_, fw) -> fw.claimed || (now - fw.lastSeenMillis) > 10_000 }
    }

    private fun findMatchingFirework(entity: Entity): TrackedFirework? {
        val now = System.currentTimeMillis()
        return recentFireworks.values
            .filter { !it.claimed && (now - it.lastSeenMillis) <= 10_000 }
            .filter { fw ->
                val dx = entity.x - fw.posX
                val dz = entity.z - fw.posZ
                val dy = entity.y - fw.highestY
                (dx * dx + dz * dz <= 16.0) && (dy in -5.0..40.0)
            }
            .minByOrNull { fw ->
                val dx = entity.x - fw.posX
                val dz = entity.z - fw.posZ
                dx * dx + dz * dz
            }
    }

    override fun processEntity(entity: Entity) {
        if (entity !is ArmorStand) {
            if (entity is FireworkRocketEntity) {
                seenThisTick.add(entity.id)
                val existingFw = recentFireworks[entity.id]
                val firework = if (existingFw != null) {
                    existingFw.highestY = maxOf(existingFw.highestY, entity.y)
                    existingFw.posX = entity.x
                    existingFw.posZ = entity.z
                    existingFw.lastSeenMillis = System.currentTimeMillis()
                    existingFw
                } else {
                    val now = System.currentTimeMillis()
                    TrackedFirework(
                        entityId = entity.id,
                        endTimeMillis = now + 180_000,
                        posX = entity.x,
                        posZ = entity.z,
                        highestY = entity.y,
                        lastSeenMillis = now,
                    ).also { recentFireworks[entity.id] = it }
                }

                if (!firework.claimed) {
                    val existingFlare = activeFlares[entity.id]
                    if (existingFlare != null) {
                        existingFlare.highestY = maxOf(existingFlare.highestY, entity.y)
                        existingFlare.posX = entity.x
                        existingFlare.posZ = entity.z
                    } else {
                        activeFlares[entity.id] = TrackedFlare(
                            entityId = entity.id,
                            flareType = FlareType.UNDEFINED,
                            endTimeMillis = firework.endTimeMillis,
                            posX = entity.x,
                            posZ = entity.z,
                            highestY = entity.y,
                        )
                    }
                }
            }
            return
        }

        val helmet = entity.getItemBySlot(EquipmentSlot.HEAD)
        if (helmet.item !is PlayerHeadItem) return

        val component = helmet[DataComponents.PROFILE] ?: return
        val textures = component.partialProfile().properties["textures"].map { it.value }
        val flareType = FlareType.entries.find { it != FlareType.UNDEFINED && textures.contains(it.texture) }
            ?: return

        seenThisTick.add(entity.id)
        val existing = activeFlares[entity.id]
        if (existing != null) {
            existing.highestY = maxOf(existing.highestY, entity.y)
            existing.posX = entity.x
            existing.posZ = entity.z
            return
        }

        val matchingFirework = findMatchingFirework(entity)
        val endTimeMillis = if (matchingFirework != null) {
            matchingFirework.claimed = true
            activeFlares.remove(matchingFirework.entityId)
            matchingFirework.endTimeMillis
        } else {
            System.currentTimeMillis() + 180_000
        }

        activeFlares[entity.id] = TrackedFlare(
            entityId = entity.id,
            flareType = flareType,
            endTimeMillis = endTimeMillis,
            posX = entity.x,
            posZ = entity.z,
            highestY = if (matchingFirework != null) maxOf(entity.y, matchingFirework.highestY) else entity.y,
        )
    }

    override fun getResult(): Deployable? {
        activeFlares.entries.removeIf { (id, _) -> !seenThisTick.contains(id) }
        if (activeFlares.isEmpty()) return null

        val playerPos = mc.player?.position()
        val inRangeFlares = if (playerPos != null) {
            activeFlares.values.filter { it.isInRange(playerPos) }
        } else {
            activeFlares.values.toList()
        }

        val pool = inRangeFlares.ifEmpty { activeFlares.values.toList() }

        val best = pool.minWithOrNull(
            compareByDescending<TrackedFlare> { it.flareType.priority }
                .thenByDescending { it.endTimeMillis }
                .thenBy { it.distanceSquared(playerPos) }
        )

        return best?.toDeployable()
    }
}
