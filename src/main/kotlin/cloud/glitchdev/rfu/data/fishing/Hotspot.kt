package cloud.glitchdev.rfu.data.fishing

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.HotSpotConstants
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.utils.World
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import java.awt.Color
import java.util.UUID
import kotlin.math.abs

data class Hotspot(
    val uuid: UUID,
    val center: Vec3,
    val buff: String,
    var radius: Float = 0f,
    val color: Color,
    val liquid: LiquidTypes,
    val startTime: Long = System.currentTimeMillis()
) {
    val blockPos = BlockPos.containing(center.x, center.y, center.z)
    var island: FishingIslands? = World.island
    var lastUpdate = System.currentTimeMillis()
    var isNotified = false
    var virtualParticleCount = 0
    var rangeEntryTime: Long? = null

    private var pendingRadius: Float = 0f
    private var lastMedianChangeTime: Long = 0

    init {
        radius = HotspotCache.getRadius(blockPos, island) ?: 0f
        pendingRadius = radius
        lastMedianChangeTime = System.currentTimeMillis()
    }

    fun addParticleDistance(distance: Double) {
        HotspotCache.addMeasurement(blockPos, distance, liquid, buff, island)
        val newMedian = HotspotCache.getMedian(blockPos, island) ?: 0f
        val now = System.currentTimeMillis()

        if (radius == 0f && newMedian > 0f) {
            radius = newMedian
            pendingRadius = newMedian
            lastMedianChangeTime = now
            HotspotCache.updateRadius(blockPos, radius, island)
        } else {
            if (abs(newMedian - pendingRadius) > HotSpotConstants.MEDIAN_STABILITY_TOLERANCE) {
                pendingRadius = newMedian
                lastMedianChangeTime = now
            } else if (now - lastMedianChangeTime >= HotSpotConstants.STABILITY_TIME_MS) {
                if (abs(radius - newMedian) > HotSpotConstants.MEDIAN_STABILITY_TOLERANCE) {
                    radius = newMedian
                    HotspotCache.updateRadius(blockPos, radius, island)
                }
            }
        }

        lastUpdate = now
    }
}
