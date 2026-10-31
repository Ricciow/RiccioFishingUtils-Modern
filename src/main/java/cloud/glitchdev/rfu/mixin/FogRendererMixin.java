package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.FogEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.util.ARGB;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
    @ModifyReturnValue(method = "setupFog", at = @At("RETURN"))
    private FogData rfu$finalFog(FogData fog, Camera camera, int renderDistanceInChunks,
                                DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level) {
        FogEvents.FinalFogEventManager.INSTANCE.getRunTasks().invoke(fog, camera, level, renderDistanceInChunks * 16.0F, deltaTracker);
        return fog;
    }

    //? if >=26.3 {
    @WrapOperation(method = "computeFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;getBaseColor(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/Camera;IF)Lorg/joml/Vector3fc;"))
    private Vector3fc rfu$fogColor(FogEnvironment environment, ClientLevel level, Camera camera, int renderDistance, float partialTicks, Operation<Vector3fc> original, @Share("rfu$fogColor") LocalRef<FogEvents.FogColorEventManager.FogColorContext> sharedContext) {
        FogEvents.FogColorEventManager.FogColorContext context = new FogEvents.FogColorEventManager.FogColorContext(camera, environment);
        FogEvents.FogColorEventManager.INSTANCE.getRunTasks().invoke(context);
        sharedContext.set(context);
        return context.getColor() != null ? ARGB.vector3fFromRGB24(context.getColor().getRGB()) : original.call(environment, level, camera, renderDistance, partialTicks);
    }
    //?} else {
    /*@WrapOperation(method = "computeFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;getBaseColor(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/Camera;IF)I"))
    private int rfu$fogColor(FogEnvironment environment, ClientLevel level, Camera camera, int renderDistance, float partialTicks, Operation<Integer> original, @Share("rfu$fogColor") LocalRef<FogEvents.FogColorEventManager.FogColorContext> sharedContext) {
        FogEvents.FogColorEventManager.FogColorContext context = new FogEvents.FogColorEventManager.FogColorContext(camera, environment);
        FogEvents.FogColorEventManager.INSTANCE.getRunTasks().invoke(context);
        sharedContext.set(context);
        return context.getColor() != null ? context.getColor().getRGB() : original.call(environment, level, camera, renderDistance, partialTicks);
    }
    *///?}

    @WrapOperation(method = "computeFogColor", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float rfu$preserveFogBrightness(float first, float second, Operation<Float> original, @Share("rfu$fogColor") LocalRef<FogEvents.FogColorEventManager.FogColorContext> sharedContext) {
        float brightestChannel = original.call(first, second);
        FogEvents.FogColorEventManager.FogColorContext context = sharedContext.get();
        // Vanilla divides by the brightest channel, turning even near-black gray white as vision adapts.
        return context != null && context.getColor() != null && context.getPreserveBrightness()
                ? Math.max(1.0F, brightestChannel) : brightestChannel;
    }
}