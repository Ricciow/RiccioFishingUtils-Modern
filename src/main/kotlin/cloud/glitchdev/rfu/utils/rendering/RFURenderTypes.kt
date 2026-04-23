package cloud.glitchdev.rfu.utils.rendering

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.renderer.RenderPipelines
//? if >= 26.1 {
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.platform.CompareOp
//?}
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderSetup

object RFURenderTypes {
    //? if >= 26.1 {
    val depth : DepthStencilState = DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)
    val color : ColorTargetState = ColorTargetState(BlendFunction.TRANSLUCENT)
    //?}

    val NO_DEPTH_QUAD_PIPELINE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("rfu/no_depth_quads")
            //? if >= 26.1 {
            .withDepthStencilState(depth)
            .withColorTargetState(color)
            //?} else {
            /*.withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            *///?}
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