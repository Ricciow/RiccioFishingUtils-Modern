package cloud.glitchdev.rfu.utils.rendering

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.renderer.RenderPipelines
//? if >= 26.1 {
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.platform.CompareOp
//?}
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderSetup
//?} else {
/*import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderStateShard
*///?}

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

    //? if >=1.21.11 {
    val TRANSLUCENT_SHAPE: RenderType = RenderType.create(
        "rfu_translucent_shape",
        RenderSetup.builder(NO_DEPTH_QUAD_PIPELINE)
            .sortOnUpload()
            .createRenderSetup()
    )
    //?} else {
    /*val TRANSLUCENT_SHAPE: RenderType = RenderType.create(
        "rfu_translucent_shape",
        1536,
        false,
        true, // sortOnUpload
        NO_DEPTH_QUAD_PIPELINE,
        RenderType.CompositeState.builder()
            .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
            .createCompositeState(false)
    )
    *///?}
}