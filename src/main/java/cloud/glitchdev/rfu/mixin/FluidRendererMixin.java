package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.FluidRenderEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
    @WrapOperation(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/FluidStateModelSet;get(Lnet/minecraft/world/level/material/FluidState;)Lnet/minecraft/client/renderer/block/FluidModel;"))
    private FluidModel rfu$fluidModel(FluidStateModelSet models, FluidState state, Operation<FluidModel> original, @Share("rfu$fluidRender") LocalRef<FluidRenderEvents.FluidRenderContext> sharedContext) {
        FluidRenderEvents.FluidRenderContext context = new FluidRenderEvents.FluidRenderContext(state, models, original.call(models, state));
        FluidRenderEvents.INSTANCE.getRunTasks().invoke(context);
        sharedContext.set(context);
        return context.getModel();
    }

    @WrapOperation(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;scaleRGB(IF)I"))
    private int rfu$fluidColor(int color, float scale, Operation<Integer> original, @Share("rfu$fluidRender") LocalRef<FluidRenderEvents.FluidRenderContext> sharedContext) {
        FluidRenderEvents.FluidRenderContext context = sharedContext.get();
        int tintColor = context != null && context.getColor() != null ? context.getColor().getRGB() : color;
        return original.call(tintColor, scale);
    }
}