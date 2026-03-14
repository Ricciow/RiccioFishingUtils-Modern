package cloud.glitchdev.rfu.utils.rendering

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.Camera
import net.minecraft.client.renderer.culling.Frustum
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderTypes
//?} else {
/*import net.minecraft.client.renderer.RenderType
*///?}

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

object Render3D {
    val camera : Camera
        get() = mc.gameRenderer.mainCamera

    fun builder(shape: Shape) = Render3DBuilder(shape)

    internal fun renderSphere(
        location: Vec3,
        radius: Float,
        color: Color,
        sliceColor: Color? = null,
        context: WorldRenderContext,
        stacks: Int = 16,
        slices: Int = 16,
        lineWidth: Float = 2.0f,
        filled: Boolean = false
    ) {
        if (!isVisible(buildSphereBounds(location, radius))) {
            return
        }

        val consumers = context.consumers()
        val camPos = camera.position()
        val vecToSphere = location.subtract(camPos)

        val matrixStack = context.matrices()

        matrixStack.pushPose()
        matrixStack.translate(
            vecToSphere.x,
            vecToSphere.y,
            vecToSphere.z
        )

        val matrix = matrixStack.last().pose()

        if (filled) {
            //? if >=1.21.11 {
            val buffer = consumers.getBuffer(RenderTypes.DEBUG_QUADS)
            //?} else {
            /*val buffer = consumers.getBuffer(RenderType.debugQuads())
            *///?}

            for (i in 0 until stacks) {
                val lat0 = Math.PI * (-0.5 + i.toDouble() / stacks)
                val z0 = sin(lat0) * radius
                val zr0 = cos(lat0) * radius

                val lat1 = Math.PI * (-0.5 + (i + 1).toDouble() / stacks)
                val z1 = sin(lat1) * radius
                val zr1 = cos(lat1) * radius

                for (j in 0 until slices) {
                    val lng0 = 2 * Math.PI * j.toDouble() / slices
                    val x0 = cos(lng0) * zr0
                    val y0 = sin(lng0) * zr0

                    val lng1 = 2 * Math.PI * (j + 1).toDouble() / slices
                    val x1 = cos(lng1) * zr0
                    val y1 = sin(lng1) * zr0

                    val x2 = cos(lng1) * zr1
                    val y2 = sin(lng1) * zr1

                    val x3 = cos(lng0) * zr1
                    val y3 = sin(lng0) * zr1

                    drawVertexSolid(buffer, matrix, x0.toFloat(), z0.toFloat(), y0.toFloat(), color, x0.toFloat() / radius, z0.toFloat() / radius, y0.toFloat() / radius)
                    drawVertexSolid(buffer, matrix, x1.toFloat(), z0.toFloat(), y1.toFloat(), color, x1.toFloat() / radius, z0.toFloat() / radius, y1.toFloat() / radius)
                    drawVertexSolid(buffer, matrix, x2.toFloat(), z1.toFloat(), y2.toFloat(), color, x2.toFloat() / radius, z1.toFloat() / radius, y2.toFloat() / radius)
                    drawVertexSolid(buffer, matrix, x3.toFloat(), z1.toFloat(), y3.toFloat(), color, x3.toFloat() / radius, z1.toFloat() / radius, y3.toFloat() / radius)
                }
            }
        }

        if (sliceColor != null) {
            //? if >=1.21.11 {
            val buffer = consumers.getBuffer(RenderTypes.LINES)
            //?} else {
            /*val buffer = consumers.getBuffer(RenderType.lines())
            *///?}

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

                    drawVertex(buffer, matrix, (x0 * zr0).toFloat(), z0.toFloat(), (y0 * zr0).toFloat(), sliceColor, lineWidth)
                    drawVertex(buffer, matrix, (x0 * zr1).toFloat(), z1.toFloat(), (y0 * zr1).toFloat(), sliceColor, lineWidth)

                    drawVertex(buffer, matrix, (x0 * zr1).toFloat(), z1.toFloat(), (y0 * zr1).toFloat(), sliceColor, lineWidth)
                    drawVertex(buffer, matrix, (x1 * zr1).toFloat(), z1.toFloat(), (y1 * zr1).toFloat(), sliceColor, lineWidth)
                }
            }
        }

        matrixStack.popPose()
    }

    internal fun renderCylinder(
        location: Vec3,
        radius: Float,
        height: Float,
        color: Color,
        context: WorldRenderContext,
        slices: Int = 32,
        borderColor: Color? = null,
        lineWidth: Float = 2.0f
    ) {
        if (!isVisible(buildCylinderBounds(location, radius, height))) {
            return
        }

        val consumers = context.consumers()
        val camPos = camera.position()
        val vecToCylinder = location.subtract(camPos)

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

                    drawVertex(lineBuffer, matrix, x0, height, z0, borderColor, lineWidth)
                    drawVertex(lineBuffer, matrix, x1, height, z1, borderColor, lineWidth)

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

    private fun isVisible(bounds: AABB): Boolean {
        val fov = mc.options.fov().get().toFloat()
        val projectionMatrix = mc.gameRenderer.getProjectionMatrix(fov)
        val quaternion = camera.rotation().conjugate(org.joml.Quaternionf())
        val viewMatrix = Matrix4f().rotation(quaternion)
        val frustum = Frustum(viewMatrix, projectionMatrix)
        val camPos = camera.position()
        frustum.prepare(camPos.x, camPos.y, camPos.z)
        return frustum.isVisible(bounds)
    }

    private fun buildSphereBounds(location: Vec3, radius: Float): AABB {
        val radiusDouble = radius.toDouble()
        return AABB(
            location.x - radiusDouble,
            location.y - radiusDouble,
            location.z - radiusDouble,
            location.x + radiusDouble,
            location.y + radiusDouble,
            location.z + radiusDouble
        )
    }

    private fun buildCylinderBounds(location: Vec3, radius: Float, height: Float): AABB {
        val radiusDouble = radius.toDouble()
        val heightDouble = height.toDouble()

        val minY = minOf(location.y, location.y + heightDouble)
        val maxY = maxOf(location.y, location.y + heightDouble)

        return AABB(
            location.x - radiusDouble,
            minY,
            location.z - radiusDouble,
            location.x + radiusDouble,
            maxY,
            location.z + radiusDouble
        )
    }

    private fun drawVertexSolid(
        buffer: VertexConsumer,
        matrix: Matrix4f,
        x: Float, y: Float, z: Float,
        color: Color,
        nx: Float = 0f, ny: Float = 1f, nz: Float = 0f
    ) {
        buffer.addVertex(matrix, x, y, z)
            .setColor(color.red, color.green, color.blue, color.alpha)
            .setNormal(nx, ny, nz)
    }
}
