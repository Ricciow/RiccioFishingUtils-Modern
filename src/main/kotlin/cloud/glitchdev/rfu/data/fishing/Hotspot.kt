package cloud.glitchdev.rfu.data.fishing

import cloud.glitchdev.rfu.constants.LiquidTypes
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
    private val particleDistances = mutableListOf<Double>()
    private val particleCount
        get() = when(liquid) {
            LiquidTypes.LAVA -> 100
            else -> 50
        }

    fun addParticleDistance(distance: Double) {
        if (particleDistances.size < particleCount) {
            particleDistances.add(distance)

            if (particleDistances.size >= 50) {
                val sorted = particleDistances.sorted()
                val percentile = sorted[(sorted.size * 0.50).toInt()]

                radius = percentile.toFloat()
            }
        }
    }

    fun isRadiusCalculated() : Boolean = particleDistances.size == particleCount
}
