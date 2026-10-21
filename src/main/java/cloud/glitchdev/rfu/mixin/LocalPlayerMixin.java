package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.FogEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @WrapOperation(method = {"aiStep", "getWaterVision"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean rfu$waterVisionFluid(LocalPlayer player, TagKey<Fluid> tag, Operation<Boolean> original) {
        if (!tag.equals(FluidTags.WATER)) return original.call(player, tag);
        FogEvents.WaterVisionFluidEventManager.WaterVisionFluidContext context = new FogEvents.WaterVisionFluidEventManager.WaterVisionFluidContext(
                player, original.call(player, tag));
        FogEvents.WaterVisionFluidEventManager.INSTANCE.getRunTasks().invoke(context);
        return context.getSubmerged();
    }
}