package cloud.glitchdev.rfu.utils.rendering

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.awt.Color
//~if >=26.1 'world.World' -> 'level.Level' {
//~if >=26.1 'World' -> 'Level' {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext

class Render3DBuilder(val shape: Shape, val context: LevelRenderContext) {
    var location: Vec3 = Vec3.ZERO
    var startLocation: Vec3 = Vec3.ZERO
    var radius: Float = 1.0f
    var color: Color = Color.WHITE
    var borderColor: Color? = null
    var height: Float = 0.0f
    var stacks: Int = 16
    var slices: Int = 16
    var lineWidth: Float = 2.0f
    var filled: Boolean = false

    var from: Vec3
        get() = startLocation
        set(value) { startLocation = value }

    var to: Vec3
        get() = location
        set(value) { location = value }

    val camera: Vec3
        get() {
            val cam = Render3D.camera
            val camPos = cam.position()

            val forward = cam.forwardVector()
            val lookX = forward.x().toDouble()
            val lookY = forward.y().toDouble()
            val lookZ = forward.z().toDouble()

            val offset = 0.5
            return camPos.add(lookX * offset, lookY * offset, lookZ * offset)
        }

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
            Shape.LINE -> Render3D.renderLine(
                startLocation, location, color, context, lineWidth
            )
        }
    }

    companion object {
        inline fun LevelRenderContext.sphere(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.SPHERE, this).apply(block).render()
        }

        inline fun LevelRenderContext.cylinder(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.CYLINDER, this).apply(block).render()
        }

        inline fun LevelRenderContext.line(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.LINE, this).apply(block).render()
        }

        inline fun build(shape: Shape, context: LevelRenderContext, block: Render3DBuilder.() -> Unit): Render3DBuilder {
            return Render3DBuilder(shape, context).apply(block)
        }
    }
}
//~}
//~}