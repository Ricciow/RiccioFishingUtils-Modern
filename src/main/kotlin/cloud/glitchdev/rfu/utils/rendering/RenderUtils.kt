package cloud.glitchdev.rfu.utils.rendering

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
//? if >=1.21.10 {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
*///?}
import net.minecraft.client.render.Camera
//? if >=1.21.11 {
/*import net.minecraft.client.render.RenderLayers
*///?} else {
import net.minecraft.client.render.RenderLayer
//?}

import net.minecraft.client.render.RenderTickCounter
import net.minecraft.client.render.VertexConsumer
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

@AutoRegister
object RenderUtils : RegisteredEvent {
    val camera : Camera
        get() = minecraft.gameRenderer.camera
    val tickCounter : RenderTickCounter
        get() = minecraft.renderTickCounter

    override fun register() {
        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            val world = minecraft.world ?: return@register

            world.entities.forEach { entity ->
                if (entity is LivingEntity && entity != minecraft.player) {
                    renderSphereOnMob(
                        entity = entity,
                        radius = 1.0f,
                        color = Color(0, 255, 255, 255),
                        true,
                        context
                    )
                }
            }
        }
    }

    private fun renderSphereOnMob(
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
        renderSphere(entityPos, radius, color, context)
    }

    private fun renderSphere(
        location: Vec3d,
        radius: Float,
        color : Color,
        context: WorldRenderContext,
    ) {
        //Consumers may be null on <1.21.10
        @Suppress("USELESS_ELVIS")
        val consumers = context.consumers() ?: return
        
        val camPos = camera.getPosition()

        val vecToSphere = location.subtract(camPos)
        val lookVec = Vec3d.fromPolar(camera.pitch, camera.yaw)
        val projection = vecToSphere.dotProduct(lookVec)

        if (projection < -radius) {
            return
        }

        //? if >=1.21.10 {
        val matrixStack = context.matrices()
        //?} else {
        /*val matrixStack = context.matrixStack() ?: return
        *///?}

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
        val stacks = 16
        val slices = 16
        val lineWidth = 2.0f

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