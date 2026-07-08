package cloud.glitchdev.rfu.utils.rendering

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.renderer.RenderPipelines
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.platform.CompareOp
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderSetup

object RFURenderTypes {
    //~ if >= 26.2 'LESS_THAN_OR_EQUAL' -> 'GREATER_THAN_OR_EQUAL' {
    val depth : DepthStencilState = DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false)
    val color : ColorTargetState = ColorTargetState(BlendFunction.TRANSLUCENT)
    //~}

    val NO_DEPTH_QUAD_PIPELINE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("rfu/no_depth_quads")
            .withDepthStencilState(depth)
            .withColorTargetState(color)
            .withCull(false)
            .build()
    )

    val TRANSLUCENT_SHAPE: RenderType = RenderType.create(
        "rfu_translucent_shape",
        RenderSetup.builder(NO_DEPTH_QUAD_PIPELINE)
            .sortOnUpload()
            .createRenderSetup()
    )
}