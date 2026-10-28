package cloud.glitchdev.rfu.utils.rendering

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import net.minecraft.world.entity.Entity
import net.minecraft.client.renderer.blockentity.BeaconRenderer
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.Vec3
import java.awt.Color
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext

class Render3DBuilder(val shape: Shape, val context: LevelRenderContext) {
    var position: Vec3 = Vec3.ZERO
    var startPosition: Vec3 = Vec3.ZERO
    var radius: Float = 1.0f
    var size: Float = if (shape == Shape.PLAYER_HEAD) 8f else 1.0f
    var skin: Identifier = DefaultPlayerSkin.getDefaultTexture()
    var showHat: Boolean = true
    var color: Color = Color.WHITE
    var borderColor: Color? = null
    var height: Float = if (shape == Shape.BEACON) BeaconRenderer.MAX_RENDER_Y.toFloat() else 0.0f
    var stacks: Int = 16
    var slices: Int = 16
    var lineWidth: Float = 2.0f
    var filled: Boolean = false
    var text: String = ""
    var seeThrough: Boolean = false
    var dropShadow: Boolean = false
    var backgroundOpacity: Float = 0.25f
    var scale: Float = 0.025f
    var scaleWithDistance: Boolean = false
    var topBorder: Boolean = true
    var bottomBorder: Boolean = true

    var from: Vec3
        get() = startPosition
        set(value) { startPosition = value }

    var to: Vec3
        get() = position
        set(value) { position = value }

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
        this.position = entityPos
    }

    fun render() {
        when (shape) {
            Shape.SPHERE -> Render3D.renderSphere(
                position, radius, color, context, stacks, slices, lineWidth, filled, borderColor, scaleWithDistance
            )
            Shape.CYLINDER -> Render3D.renderCylinder(
                position, radius, height, color, context, slices, borderColor, lineWidth, scaleWithDistance, topBorder, bottomBorder
            )
            Shape.CUBE -> Render3D.renderCube(
                position, size, color, context, filled, borderColor, lineWidth, scaleWithDistance
            )
            Shape.BEACON -> Render3D.renderBeacon(position, color, context, height.toInt())
            Shape.PLAYER_HEAD -> Render3D.renderPlayerHead(
                position, skin, context, size, scale, showHat, seeThrough, scaleWithDistance
            )
            Shape.LINE -> Render3D.renderLine(
                startPosition, position, color, context, lineWidth, scaleWithDistance
            )
            Shape.TEXT -> Render3D.renderText(
                position, text, color, context, scale, seeThrough, dropShadow, backgroundOpacity, scaleWithDistance
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

        inline fun LevelRenderContext.cube(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.CUBE, this).apply(block).render()
        }

        inline fun LevelRenderContext.beacon(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.BEACON, this).apply(block).render()
        }

        inline fun LevelRenderContext.playerHead(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.PLAYER_HEAD, this).apply(block).render()
        }

        inline fun LevelRenderContext.line(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.LINE, this).apply(block).render()
        }

        inline fun LevelRenderContext.text(block: Render3DBuilder.() -> Unit) {
            Render3DBuilder(Shape.TEXT, this).apply(block).render()
        }

        inline fun build(shape: Shape, context: LevelRenderContext, block: Render3DBuilder.() -> Unit): Render3DBuilder {
            return Render3DBuilder(shape, context).apply(block)
        }
    }
}
