package cloud.glitchdev.rfu.utils.rendering

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import net.minecraft.client.renderer.RenderPipelines
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderSetup
//?} else {
/*import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderStateShard
*///?}

object RFURenderTypes {
    val NO_DEPTH_QUAD_PIPELINE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("rfu/no_depth_quads")
            .withDepthWrite(false)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
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