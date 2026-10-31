package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.FovEvents;
import cloud.glitchdev.rfu.events.managers.FogEvents;
import cloud.glitchdev.rfu.access.CameraFluidStateAccess;
import cloud.glitchdev.rfu.events.wrappers.Cancelable;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin implements CameraFluidStateAccess {
    @Unique private FluidState rfu$fogFluidState;
    @Unique private boolean rfu$skipFluidFog;

    @WrapOperation(method = "getFluidInCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
    private FluidState rfu$sampleFogFluid(Level level, BlockPos pos, Operation<FluidState> original) {
        FluidState state = original.call(level, pos);
        this.rfu$fogFluidState = state;
        this.rfu$skipFluidFog = false;
        return state;
    }

    @ModifyReturnValue(method = "getFluidInCamera", at = @At("RETURN"))
    private FogType rfu$rememberFogFluid(FogType type) {
        if (type != FogType.WATER && type != FogType.LAVA) {
            this.rfu$fogFluidState = null;
            return type;
        }
        return this.rfu$skipFluidFog ? FogType.NONE : type;
    }

    @Override
    public FluidState rfu$getFogFluidState() {
        return this.rfu$fogFluidState;
    }

    @WrapOperation(method = "getFluidInCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean rfu$cameraFluid(FluidState state, TagKey<Fluid> tag, Operation<Boolean> original) {
        FogEvents.CameraFluidEventManager.CameraFluidContext context = new FogEvents.CameraFluidEventManager.CameraFluidContext(
                (Camera) (Object) this, state, tag, original.call(state, tag));
        FogEvents.CameraFluidEventManager.INSTANCE.getRunTasks().invoke(context);
        this.rfu$skipFluidFog = context.getDisableFog();
        return context.getMatches();
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void onCalculateFov(float partialTicks, CallbackInfoReturnable<Float> cir) {
        Cancelable<Float> cancelable = new Cancelable<>(cir, null);
        FovEvents.INSTANCE.getRunTasks().invoke(cancelable, partialTicks);
    }
}