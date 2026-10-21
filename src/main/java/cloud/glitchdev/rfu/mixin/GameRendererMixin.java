package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.FogEvents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @WrapOperation(method = "extractCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lnet/minecraft/client/renderer/fog/FogData;"))
    private FogData rfu$finalFog(FogRenderer renderer, Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level, Operation<FogData> original) {
        FogData fog = original.call(renderer, camera, renderDistanceInChunks, deltaTracker, darkenWorldAmount, level);
        FogEvents.FinalFogEventManager.INSTANCE.getRunTasks().invoke(fog, camera, level, renderDistanceInChunks * 16.0F, deltaTracker);
        return fog;
    }
}