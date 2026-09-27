package cloud.glitchdev.rfu.utils.rendering

//? if >=26.3 {
import com.mojang.renderpearl.api.pipeline.BlendFunction
import com.mojang.renderpearl.api.pipeline.RenderPipeline
import com.mojang.renderpearl.api.pipeline.ColorTargetState
import com.mojang.renderpearl.api.pipeline.DepthStencilState
import com.mojang.renderpearl.api.pipeline.CompareOp
//?} else {
/*import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.platform.CompareOp
*///?}
import net.minecraft.client.renderer.RenderPipelines
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