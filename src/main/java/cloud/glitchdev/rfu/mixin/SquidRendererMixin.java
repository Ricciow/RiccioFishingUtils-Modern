package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.access.PlhlegblastStateAccess;
import cloud.glitchdev.rfu.utils.MobUtils;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.state.SquidRenderState;

//~ if <1.21.11 'squid.Squid' -> 'Squid' {
import net.minecraft.world.entity.animal.squid.Squid;
//~}
//~ if <1.21.11 'Identifier' -> 'ResourceLocation' {
import net.minecraft.resources.Identifier;
//~}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SquidRenderer.class)
public class SquidRendererMixin {

    @Unique
    //~ if <1.21.11 'Identifier' -> 'ResourceLocation' {
    private static final Identifier rfu$GLOW_SQUID_TEXTURE = Identifier.withDefaultNamespace("textures/entity/squid/glow_squid.png");
    //~}

    //~ if <1.21.11 'squid/Squid;' -> 'Squid;' {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/animal/squid/Squid;Lnet/minecraft/client/renderer/entity/state/SquidRenderState;F)V",
            at = @At("TAIL")
    )
    //~}
    private void rfu$extractRenderState(Squid squid, SquidRenderState renderState, float f, CallbackInfo ci) {
        ((PlhlegblastStateAccess) renderState).rfu$setPlhlegblast(MobUtils.INSTANCE.isPlhlegblast(squid));
    }

    //~ if <1.21.11 'Identifier' -> 'ResourceLocation' {
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/SquidRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void rfu$getTextureLocation(SquidRenderState renderState, CallbackInfoReturnable<Identifier> cir) {
        if (((PlhlegblastStateAccess) renderState).rfu$isPlhlegblast()) {
            cir.setReturnValue(rfu$GLOW_SQUID_TEXTURE);
        }
    }
    //~}
}