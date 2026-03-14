package cloud.glitchdev.rfu.utils.rendering

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.awt.Color

class Render3DBuilder(val shape: Shape) {
    private var location: Vec3 = Vec3.ZERO
    private var radius: Float = 1.0f
    private var color: Color = Color.WHITE
    private var borderColor: Color? = null
    private var height: Float = 0.0f
    private var stacks: Int = 16
    private var slices: Int = 16
    private var lineWidth: Float = 2.0f
    private var filled: Boolean = false
    private var drawSlices: Boolean = true

    fun pos(location: Vec3) = apply { this.location = location }

    fun pos(entity: Entity, centered: Boolean = false) = apply {
        val tickDelta = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
        var entityPos = entity.getPosition(tickDelta)
        if (centered) {
            entityPos = entityPos.add(0.0, (entity.bbHeight / 2).toDouble(), 0.0)
        }
        this.location = entityPos
    }

    fun radius(radius: Float) = apply { this.radius = radius }
    fun color(color: Color) = apply { this.color = color }
    fun borderColor(borderColor: Color?) = apply { this.borderColor = borderColor }
    fun sliceColor(borderColor: Color?) = apply { this.borderColor = borderColor }
    fun height(height: Float) = apply { this.height = height }
    fun stacks(stacks: Int) = apply { this.stacks = stacks }
    fun slices(slices: Int) = apply { this.slices = slices }
    fun lineWidth(lineWidth: Float) = apply { this.lineWidth = lineWidth }
    fun filled(filled: Boolean) = apply { this.filled = filled }

    fun render(context: WorldRenderContext) {
        when (shape) {
            Shape.SPHERE -> Render3D.renderSphere(
                location, radius, color, borderColor, context, stacks, slices, lineWidth, filled
            )
            Shape.CYLINDER -> Render3D.renderCylinder(
                location, radius, height, color, context, slices, borderColor, lineWidth
            )
        }
    }
}
