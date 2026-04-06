package cloud.glitchdev.rfu.data.fishing

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.utils.World
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import java.awt.Color
import java.util.UUID

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

    init {
        radius = HotspotCache.getMedian(blockPos, island) ?: 0f
    }

    fun addParticleDistance(distance: Double) {
        HotspotCache.addMeasurement(blockPos, distance, liquid, buff, island)
        radius = HotspotCache.getMedian(blockPos, island) ?: 0f
        lastUpdate = System.currentTimeMillis()
    }

    fun isRadiusCalculated() : Boolean = radius > 0f
}
