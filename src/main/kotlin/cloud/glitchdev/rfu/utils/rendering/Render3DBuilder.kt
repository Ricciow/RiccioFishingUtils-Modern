package cloud.glitchdev.rfu.utils.rendering

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.awt.Color

class Render3DBuilder(val shape: Shape, val context: WorldRenderContext) {
    var location: Vec3 = Vec3.ZERO
    var radius: Float = 1.0f
    var color: Color = Color.WHITE
    var borderColor: Color? = null
    var height: Float = 0.0f
    var stacks: Int = 16
    var slices: Int = 16
    var lineWidth: Float = 2.0f
    var filled: Boolean = false

    fun pos(entity: Entity, centered: Boolean = false) = apply {
        val tickDelta = mc.deltaTracker.getGameTimeDeltaPartialTick(true)
        var entityPos = entity.getPosition(tickDelta)
        if (centered) {
            entityPos = entityPos.add(0.0, (entity.bbHeight / 2).toDouble(), 0.0)
        }
        this.location = entityPos
    }

    fun render() {
        when (shape) {
            Shape.SPHERE -> Render3D.renderSphere(
                location, radius, color, context, stacks, slices, lineWidth, filled, borderColor
            )
            Shape.CYLINDER -> Render3D.renderCylinder(
                location, radius, height, color, context, slices, borderColor, lineWidth
            )
        }
    }

    companion object {
        inline fun WorldRenderContext.sphere(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.SPHERE, this).apply(block).render()
        }

        inline fun WorldRenderContext.cylinder(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.CYLINDER, this).apply(block).render()
        }

        inline fun build(shape: Shape, context: WorldRenderContext, block: Render3DBuilder.() -> Unit): Render3DBuilder {
            return Render3DBuilder(shape, context).apply(block)
        }
    }
}
