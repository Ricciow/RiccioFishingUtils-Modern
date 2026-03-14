package cloud.glitchdev.rfu.data.fishing

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
    val lava: Boolean,
    val startTime: Long = System.currentTimeMillis()
) {
    private val particleDistances = mutableListOf<Double>()

    fun addParticleDistance(distance: Double) {
        if (particleDistances.size < 200) {
            particleDistances.add(distance)

            if (particleDistances.size >= 10) {
                val sorted = particleDistances.sorted()
                val percentile95 = sorted[(sorted.size * 0.95).toInt()]
                
                var calculatedRadius = percentile95.toFloat()

                if (abs(calculatedRadius - 4.0f) < 0.5f) {
                    calculatedRadius = 4.0f
                }
                
                radius = calculatedRadius
            } else {
                radius = particleDistances.maxOrNull()?.toFloat() ?: 0f
            }
        }
    }

    fun isRadiusCalculated() : Boolean = particleDistances.size >= 10
}
