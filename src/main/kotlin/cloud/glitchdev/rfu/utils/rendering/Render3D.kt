package cloud.glitchdev.rfu.utils.rendering

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.Camera
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderTypes
//?} else {
/*import net.minecraft.client.renderer.RenderType
*///?}

import net.minecraft.client.DeltaTracker
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

object Render3D {
    val camera : Camera
        get() = mc.gameRenderer.mainCamera
    val tickCounter : DeltaTracker
        get() = mc.deltaTracker

    fun renderSphereOnMob(
        entity: Entity,
        radius: Float,
        color : Color,
        centered : Boolean = false,
        context : WorldRenderContext
    ) {
        val tickDelta = tickCounter.getGameTimeDeltaPartialTick(true)
        var entityPos = entity.getPosition(tickDelta)
        if(centered) {
            entityPos = entityPos.add(0.0, (entity.bbHeight /2).toDouble(), 0.0)
        }
        renderSphere(entityPos, radius, color, context, 32, 32)
    }

    fun renderSphere(
        location: Vec3,
        radius: Float,
        color : Color,
        context: WorldRenderContext,
        stacks : Int = 16,
        slices : Int = 16,
        lineWidth : Float = 2.0f
    ) {
        val consumers = context.consumers()
        val camPos = camera.position()
        val vecToSphere = location.subtract(camPos)
        //? if >=1.21.11 {
        val lookVec = Vec3.directionFromRotation(camera.xRot(), camera.yRot())
        //?} else {
        /*val lookVec = Vec3.directionFromRotation(camera.xRot, camera.yRot)
        *///?}
        val projection = vecToSphere.dot(lookVec)

        if (projection < -radius) {
            return
        }

        val matrixStack = context.matrices()

        matrixStack.pushPose()
        matrixStack.translate(
            vecToSphere.x,
            vecToSphere.y,
            vecToSphere.z
        )

        //? if >=1.21.11 {
        val buffer = consumers.getBuffer(RenderTypes.LINES)
        //?} else {
        /*val buffer = consumers.getBuffer(RenderType.lines())
        *///?}

        val matrix = matrixStack.last().pose()

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

        matrixStack.popPose()
    }

    fun renderDisk(
        location: Vec3,
        radius: Float,
        height: Float,
        color: Color,
        context: WorldRenderContext,
        slices: Int = 32,
        borderColor: Color? = null,
        lineWidth: Float = 2.0f
    ) {
        val consumers = context.consumers()
        val camPos = camera.position()
        val vecToCylinder = location.subtract(camPos)

        //? if >=1.21.11 {
        val lookVec = Vec3.directionFromRotation(camera.xRot(), camera.yRot())
        //?} else {
        /*val lookVec = Vec3.directionFromRotation(camera.xRot, camera.yRot)
        *///?}
        val projection = vecToCylinder.dot(lookVec)

        if (projection < -(radius + height)) {
            return
        }

        val matrixStack = context.matrices()
        matrixStack.pushPose()

        try {
            matrixStack.translate(
                vecToCylinder.x,
                vecToCylinder.y,
                vecToCylinder.z
            )
            val matrix = matrixStack.last().pose()

            //? if >=1.21.11 {
            val solidBuffer = consumers.getBuffer(RenderTypes.DEBUG_QUADS)
            //?} else {
            /*val solidBuffer = consumers.getBuffer(RenderType.debugQuads())
            *///?}

            for (i in 0 until slices) {
                val angle0 = 2 * Math.PI * i.toDouble() / slices
                val angle1 = 2 * Math.PI * (i + 1).toDouble() / slices

                val x0 = (cos(angle0) * radius).toFloat()
                val z0 = (sin(angle0) * radius).toFloat()
                val x1 = (cos(angle1) * radius).toFloat()
                val z1 = (sin(angle1) * radius).toFloat()

                drawVertexSolid(solidBuffer, matrix, 0f, height, 0f, color)
                drawVertexSolid(solidBuffer, matrix, x0, height, z0, color)
                drawVertexSolid(solidBuffer, matrix, x1, height, z1, color)
                drawVertexSolid(solidBuffer, matrix, 0f, height, 0f, color)

                drawVertexSolid(solidBuffer, matrix, 0f, 0f, 0f, color)
                drawVertexSolid(solidBuffer, matrix, x1, 0f, z1, color)
                drawVertexSolid(solidBuffer, matrix, x0, 0f, z0, color)
                drawVertexSolid(solidBuffer, matrix, 0f, 0f, 0f, color)

                drawVertexSolid(solidBuffer, matrix, x0, 0f, z0, color)
                drawVertexSolid(solidBuffer, matrix, x1, 0f, z1, color)
                drawVertexSolid(solidBuffer, matrix, x1, height, z1, color)
                drawVertexSolid(solidBuffer, matrix, x0, height, z0, color)
            }

            if (borderColor != null) {
                //? if >=1.21.11 {
                val lineBuffer = consumers.getBuffer(RenderTypes.LINES)
                //?} else {
                /*val lineBuffer = consumers.getBuffer(RenderType.lines())
                *///?}

                for (i in 0 until slices) {
                    val angle0 = 2 * Math.PI * i.toDouble() / slices
                    val angle1 = 2 * Math.PI * (i + 1).toDouble() / slices

                    val x0 = (cos(angle0) * radius).toFloat()
                    val z0 = (sin(angle0) * radius).toFloat()
                    val x1 = (cos(angle1) * radius).toFloat()
                    val z1 = (sin(angle1) * radius).toFloat()

                    // Top circle border segment
                    drawVertex(lineBuffer, matrix, x0, height, z0, borderColor, lineWidth)
                    drawVertex(lineBuffer, matrix, x1, height, z1, borderColor, lineWidth)

                    // Bottom circle border segment
                    drawVertex(lineBuffer, matrix, x0, 0f, z0, borderColor, lineWidth)
                    drawVertex(lineBuffer, matrix, x1, 0f, z1, borderColor, lineWidth)
                }
            }

        } finally {
            matrixStack.popPose()
        }
    }

    private fun drawVertex(
        buffer: VertexConsumer,
        matrix: Matrix4f,
        x: Float, y: Float, z: Float,
        color: Color,
        @Suppress("unused")
        lineWidth: Float
    ) {
        buffer.addVertex(matrix, x, y, z)
            .setColor(color.red, color.green, color.blue, color.alpha)
            .setNormal(1f, 0f, 0f)
        //? if >=1.21.11 {
            .setLineWidth(lineWidth)
        //?}
    }

    private fun drawVertexSolid(
        buffer: VertexConsumer,
        matrix: Matrix4f,
        x: Float, y: Float, z: Float,
        color: Color
    ) {
        buffer.addVertex(matrix, x, y, z)
            .setColor(color.red, color.green, color.blue, color.alpha)
            .setNormal(0f, 1f, 0f)
    }
}