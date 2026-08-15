package cloud.glitchdev.rfu.utils.rendering

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import net.minecraft.client.Camera
import net.minecraft.client.renderer.rendertype.RenderTypes
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
//?if < 26.2 {
import net.minecraft.client.renderer.culling.Frustum
//?}

object Render3D {
    val camera : Camera
        get() = 
        //? if >=26.2 {
        mc.gameRenderer.mainCamera()
        //?} else {
        /*mc.gameRenderer.mainCamera
        *///?}

    fun builder(shape: Shape, context: LevelRenderContext) = Render3DBuilder(shape, context)

    inline fun draw(context: LevelRenderContext, block: LevelRenderContext.() -> Unit) {
        context.apply(block)
    }

    internal fun renderSphere(
        location: Vec3,
        radius: Float,
        color: Color,
        context: LevelRenderContext,
        stacks: Int = 16,
        slices: Int = 16,
        lineWidth: Float = 2.0f,
        filled: Boolean = false,
        borderColor: Color? = null,
        scaleWithDistance: Boolean = false
    ) {
        if (!isVisible(buildSphereBounds(location, radius))) {
            return
        }

        //? if >=26.2 {
        val matrixStack = context.poseStack()
        //?} else {
        /*val consumers = context.bufferSource()
        val matrixStack = context.poseStack()
        *///?}

        val camPos = camera.position()
        val vecToSphere = location.subtract(camPos)

        matrixStack.pushPose()
        matrixStack.translate(
            vecToSphere.x,
            vecToSphere.y,
            vecToSphere.z
        )

        val matrix = Matrix4f(matrixStack.last().pose())

        if (filled) {
            //? if >=26.2 {
            context.submitNodeCollector().submitCustomGeometry(matrixStack, RFURenderTypes.TRANSLUCENT_SHAPE) { _, buffer ->
            //?} else {
            /*val buffer = consumers.getBuffer(RFURenderTypes.TRANSLUCENT_SHAPE)
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
            //? if >=26.2 {
            }
            //?}
        }

        if (borderColor != null) {
            val distance = vecToSphere.length()
            val finalLineWidth = if (scaleWithDistance) {
                lineWidth * maxOf(1f, 10f / maxOf(1f, distance.toFloat()))
            } else {
                lineWidth
            }

            //? if >=26.2 {
            context.submitNodeCollector().submitCustomGeometry(matrixStack, RenderTypes.LINES) { _, buffer ->
            //?} else {
            /*val buffer = consumers.getBuffer(RenderTypes.LINES)
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

                    drawLine(buffer, matrix, (x0 * zr0).toFloat(), z0.toFloat(), (y0 * zr0).toFloat(), (x0 * zr1).toFloat(), z1.toFloat(), (y0 * zr1).toFloat(), borderColor, finalLineWidth)
                    drawLine(buffer, matrix, (x0 * zr1).toFloat(), z1.toFloat(), (y0 * zr1).toFloat(), (x1 * zr1).toFloat(), z1.toFloat(), (y1 * zr1).toFloat(), borderColor, finalLineWidth)
                }
            }
            //? if >=26.2 {
            }
            //?}
        }

        matrixStack.popPose()
    }

    internal fun renderCylinder(
        location: Vec3,
        radius: Float,
        height: Float,
        color: Color,
        context: LevelRenderContext,
        slices: Int = 32,
        borderColor: Color? = null,
        lineWidth: Float = 2.0f,
        scaleWithDistance: Boolean = false,
        topBorder: Boolean = true,
        bottomBorder: Boolean = true
    ) {
        if (!isVisible(buildCylinderBounds(location, radius, height))) {
            return
        }

        //? if >=26.2 {
        val matrixStack = context.poseStack()
        //?} else {
        /*val consumers = context.bufferSource()
        val matrixStack = context.poseStack()
        *///?}

        val camPos = camera.position()
        val vecToCylinder = location.subtract(camPos)

        matrixStack.pushPose()

        try {
            matrixStack.translate(
                vecToCylinder.x,
                vecToCylinder.y,
                vecToCylinder.z
            )
            val matrix = Matrix4f(matrixStack.last().pose())

            //? if >=26.2 {
            context.submitNodeCollector().submitCustomGeometry(matrixStack, RFURenderTypes.TRANSLUCENT_SHAPE) { _, solidBuffer ->
            //?} else {
            /*val solidBuffer = consumers.getBuffer(RFURenderTypes.TRANSLUCENT_SHAPE)
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
            //? if >=26.2 {
            }
            //?}

            if (borderColor != null && (topBorder || bottomBorder)) {
                val distance = vecToCylinder.length()
                val finalLineWidth = if (scaleWithDistance) {
                    lineWidth * maxOf(1f, 10f / maxOf(1f, distance.toFloat()))
                } else {
                    lineWidth
                }

                //? if >=26.2 {
                context.submitNodeCollector().submitCustomGeometry(matrixStack, RenderTypes.LINES) { _, lineBuffer ->
                //?} else {
                /*val lineBuffer = consumers.getBuffer(RenderTypes.LINES)
                *///?}

                val topY = maxOf(0f, height)
                val bottomY = minOf(0f, height)

                for (i in 0 until slices) {
                    val angle0 = 2 * Math.PI * i.toDouble() / slices
                    val angle1 = 2 * Math.PI * (i + 1).toDouble() / slices

                    val x0 = (cos(angle0) * radius).toFloat()
                    val z0 = (sin(angle0) * radius).toFloat()
                    val x1 = (cos(angle1) * radius).toFloat()
                    val z1 = (sin(angle1) * radius).toFloat()

                    if (topBorder) {
                        drawLine(lineBuffer, matrix, x0, topY, z0, x1, topY, z1, borderColor, finalLineWidth)
                    }
                    if (bottomBorder) {
                        drawLine(lineBuffer, matrix, x0, bottomY, z0, x1, bottomY, z1, borderColor, finalLineWidth)
                    }
                }
                //? if >=26.2 {
                }
                //?}
            }

        } finally {
            matrixStack.popPose()
        }
    }

    internal fun renderLine(
        start: Vec3,
        end: Vec3,
        color: Color,
        context: LevelRenderContext,
        lineWidth: Float = 2.0f,
        scaleWithDistance: Boolean = false
    ) {
        //? if >=26.2 {
        val matrixStack = context.poseStack()
        //?} else {
        /*val consumers = context.bufferSource()
        val matrixStack = context.poseStack()
        *///?}

        val camPos = camera.position()
        val relStart = start.subtract(camPos)
        val relEnd = end.subtract(camPos)
        val mid = relStart.add(relEnd).scale(0.5)
        val distance = mid.length()

        val finalLineWidth = if (scaleWithDistance) {
            lineWidth * maxOf(1f, 10f / maxOf(1f, distance.toFloat()))
        } else {
            lineWidth
        }

        matrixStack.pushPose()
        val matrix = Matrix4f(matrixStack.last().pose())
        
        //? if >=26.2 {
        context.submitNodeCollector().submitCustomGeometry(matrixStack, RenderTypes.LINES) { _, buffer ->
            drawLine(buffer, matrix, relStart.x.toFloat(), relStart.y.toFloat(), relStart.z.toFloat(), relEnd.x.toFloat(), relEnd.y.toFloat(), relEnd.z.toFloat(), color, finalLineWidth)
        }
        //?} else {
        /*val buffer = consumers.getBuffer(RenderTypes.LINES)
        drawLine(buffer, matrix, relStart.x.toFloat(), relStart.y.toFloat(), relStart.z.toFloat(), relEnd.x.toFloat(), relEnd.y.toFloat(), relEnd.z.toFloat(), color, finalLineWidth)
        *///?}

        matrixStack.popPose()
    }

    private fun drawLine(
        buffer: VertexConsumer,
        matrix: Matrix4f,
        x0: Float, y0: Float, z0: Float,
        x1: Float, y1: Float, z1: Float,
        color: Color,
        lineWidth: Float
    ) {
        val dx = x1 - x0
        val dy = y1 - y0
        val dz = z1 - z0
        val len = sqrt(dx * dx + dy * dy + dz * dz)
        val nx = if (len != 0f) dx / len else 1f
        val ny = if (len != 0f) dy / len else 0f
        val nz = if (len != 0f) dz / len else 0f

        drawVertex(buffer, matrix, x0, y0, z0, color, lineWidth, nx, ny, nz)
        drawVertex(buffer, matrix, x1, y1, z1, color, lineWidth, nx, ny, nz)
    }

    private fun drawVertex(
        buffer: VertexConsumer,
        matrix: Matrix4f,
        x: Float, y: Float, z: Float,
        color: Color,
        @Suppress("unused")
        lineWidth: Float,
        nx: Float = 1f, ny: Float = 0f, nz: Float = 0f
    ) {
        buffer.addVertex(matrix, x, y, z)
            .setColor(color.red, color.green, color.blue, color.alpha)
            .setNormal(nx, ny, nz)
            .setLineWidth(lineWidth)
    }

    private fun isVisible(bounds: AABB): Boolean {
        //? if >=26.2 {
        return mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.cullFrustum.isVisible(bounds)
        //?} else {
        /*val projectionMatrix = mc.gameRenderer.gameRenderState.levelRenderState.cameraRenderState.projectionMatrix

        val quaternion = camera.rotation().conjugate(org.joml.Quaternionf())
        val viewMatrix = Matrix4f().rotation(quaternion)
        val frustum = Frustum(viewMatrix, projectionMatrix)
        val camPos = camera.position()
        frustum.prepare(camPos.x, camPos.y, camPos.z)
        return frustum.isVisible(bounds)
        *///?}
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

    fun renderText(
        location: Vec3,
        text: String,
        color: Color,
        context: LevelRenderContext,
        scale: Float = 0.025f,
        seeThrough: Boolean = false,
        dropShadow: Boolean = false,
        backgroundOpacity: Float = 0.25f,
        scaleWithDistance: Boolean = false
    ) {
        if (text.isEmpty()) return

        //? if >=26.2 {
        val matrixStack = context.poseStack()
        val collector = context.submitNodeCollector()
        //?} else {
        /*val consumers = context.bufferSource()
        val matrixStack = context.poseStack()
        *///?}

        val camPos = camera.position()
        val vecToText = location.subtract(camPos)
        val distance = vecToText.length()

        val finalScale = if (scaleWithDistance) {
            scale * maxOf(1f, distance.toFloat() / 10f)
        } else {
            scale
        }

        matrixStack.pushPose()
        matrixStack.translate(
            vecToText.x,
            vecToText.y,
            vecToText.z
        )
        matrixStack.mulPose(camera.rotation())
        matrixStack.scale(finalScale, -finalScale, finalScale)

        val font = mc.font
        val lines = text.lines()
        val startY = -((lines.size - 1) * font.lineHeight) / 2f

        val backgroundAlpha = (backgroundOpacity * 255).toInt().coerceIn(0, 255)
        val backgroundColorInt = (backgroundAlpha shl 24) or 0x000000

        for ((index, line) in lines.withIndex()) {
            val lineComp = Component.literal(line)
            val charSequence = lineComp.getVisualOrderText()
            val x = -font.width(lineComp) / 2f
            val y = startY + index * font.lineHeight

            //? if >=26.2 {
            val displayMode = if (seeThrough) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL
            collector.submitText(
                matrixStack,
                x,
                y,
                charSequence,
                dropShadow,
                displayMode,
                0xF000F0,
                color.rgb,
                backgroundColorInt,
                0
            )
            //?} else {
            /*val displayMode = if (seeThrough) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL
            val matrix = matrixStack.last().pose()
            font.drawInBatch(
                charSequence,
                x,
                y,
                color.rgb,
                dropShadow,
                matrix,
                consumers,
                displayMode,
                backgroundColorInt,
                0xF000F0
            )
            *///?}
        }

        matrixStack.popPose()
    }
}