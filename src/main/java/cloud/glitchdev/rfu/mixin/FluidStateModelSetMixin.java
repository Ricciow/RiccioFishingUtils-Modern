package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.FluidRenderEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidStateModelSet.class)
public class FluidStateModelSetMixin {
    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private FluidModel rfu$fluidModel(FluidModel original, FluidState state) {
        FluidRenderEvents.FluidRenderContext context = new FluidRenderEvents.FluidRenderContext(state, (FluidStateModelSet) (Object) this, original);
        FluidRenderEvents.INSTANCE.getRunTasks().invoke(context);
        FluidModel model = context.getModel();
        if (context.getColor() == null) return model;

        return new FluidModel(
                model.layer(),
                model.stillMaterial(),
                model.flowingMaterial(),
                model.overlayMaterial(),
                BlockTintSources.constant(context.getColor().getRGB())
        );
    }
}
