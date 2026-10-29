package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.FluidRenderEvents;
import cloud.glitchdev.rfu.utils.rendering.FluidTintCache;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(FluidStateModelSet.class)
public class FluidStateModelSetMixin {
    @Shadow @Final private Map<Fluid, FluidModel> modelByFluid;
    @Shadow @Final private FluidModel missingModel;
    @Unique private final FluidTintCache rfu$tintCache = new FluidTintCache();

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private FluidModel rfu$fluidModel(FluidModel original, FluidState state) {
        FluidRenderEvents.FluidRenderContext context = new FluidRenderEvents.FluidRenderContext(
                state, original, fluidState -> this.modelByFluid.getOrDefault(fluidState.getType(), this.missingModel));
        FluidRenderEvents.INSTANCE.getRunTasks().invoke(context);
        return this.rfu$tintCache.get(state.getType(), context.getModel(), context.getColor(), context.getOpacity());
    }
}
