package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.other.HideNearbyPlayers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @WrapOperation(method = "extractShadow(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/Minecraft;Lnet/minecraft/world/level/Level;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getShadowRadius(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)F"))
    private float rfu$playerShadow(EntityRenderer<?, ?> renderer, EntityRenderState state, Operation<Float> original) {
        return HideNearbyPlayers.INSTANCE.shouldRenderModel(state) ? original.call(renderer, state) : 0.0F;
    }
}