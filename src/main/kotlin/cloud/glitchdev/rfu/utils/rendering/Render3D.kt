package cloud.glitchdev.rfu.utils.rendering

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.render.Camera
//? if >=1.21.11 {
/*import net.minecraft.client.render.RenderLayers
*///?} else {
import net.minecraft.client.render.RenderLayer
//?}

import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.render.VertexConsumer
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

object Render3D {
    val camera : Camera
        get() = minecraft.gameRenderer.camera
    val tickCounter : RenderTickCounter
        get() = minecraft.renderTickCounter

    fun renderSphereOnMob(
        entity: Entity,
        radius: Float,
        color : Color,
        centered : Boolean = false,
        context : WorldRenderContext
    ) {
        val tickDelta = tickCounter.getTickProgress(true)
        var entityPos = entity.getLerpedPos(tickDelta)
        if(centered) {
            entityPos = entityPos.add(0.0, (entity.height/2).toDouble(), 0.0)
        }
        renderSphere(entityPos, radius, color, context, 32, 32)
    }

    fun renderSphere(
        location: Vec3d,
        radius: Float,
        color : Color,
        context: WorldRenderContext,
        stacks : Int = 16,
        slices : Int = 16,
        lineWidth : Float = 2.0f
    ) {
        val consumers = context.consumers()
        val camPos = camera.cameraPos
        val vecToSphere = location.subtract(camPos)
        val lookVec = Vec3d.fromPolar(camera.pitch, camera.yaw)
        val projection = vecToSphere.dotProduct(lookVec)

        if (projection < -radius) {
            return
        }

        val matrixStack = context.matrices()

        matrixStack.push()
        matrixStack.translate(
            vecToSphere.x,
            vecToSphere.y,
            vecToSphere.z
        )

        //? if >=1.21.11 {
        /*val buffer = consumers.getBuffer(RenderLayers.LINES)
        *///?} else {
        val buffer = consumers.getBuffer(RenderLayer.getLines())
        //?}

        val matrix = matrixStack.peek().positionMatrix

        for (i in 0 until stacks) {
            val lat0 = Math.PI * (-0.5 + (i.toDouble() - 1) / stacks)
            val z0 = sin(lat0) * radius
            val zr0 = cos(lat0) * radius

            val lat1 = Math.PI * (-0.5 + i.toDouble() / stacks)
            val z1 = sin(lat1) * radius
            val zr1 = cos(lat1) * radius

            for (j in 0 until slices) {
                val lng0 = 2 * Math.PI * (j - 1).toDouble() / slices
                val x0 = cos(lng0)
                val y0 = sin(lng0)

                val lng1 = 2 * Math.PI * j.toDouble() / slices
                val x1 = cos(lng1)
                val y1 = sin(lng1)

                drawVertex(buffer, matrix, (x0 * zr0).toFloat(), z0.toFloat(), (y0 * zr0).toFloat(), color, lineWidth)
                drawVertex(buffer, matrix, (x0 * zr1).toFloat(), z1.toFloat(), (y0 * zr1).toFloat(), color, lineWidth)

                drawVertex(buffer, matrix, (x0 * zr1).toFloat(), z1.toFloat(), (y0 * zr1).toFloat(), color, lineWidth)
                drawVertex(buffer, matrix, (x1 * zr1).toFloat(), z1.toFloat(), (y1 * zr1).toFloat(), color, lineWidth)
            }
        }

        matrixStack.pop()
    }

    private fun drawVertex(
        buffer: VertexConsumer,
        matrix: Matrix4f,
        x: Float, y: Float, z: Float,
        color: Color,
        @Suppress("unused")
        lineWidth: Float
    ) {
        buffer.vertex(matrix, x, y, z)
            .color(color.red, color.green, color.blue, color.alpha)
            .normal(1f, 0f, 0f)
        //? if >=1.21.11 {
            /*.lineWidth(lineWidth)
        *///?}
    }
}