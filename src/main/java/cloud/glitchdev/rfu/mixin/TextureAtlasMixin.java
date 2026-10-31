package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.feature.fishing.CustomLiquidTextures;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
    @Inject(method = "uploadAnimationFrames", at = @At("HEAD"))
    private void rfu$refreshLiquidTextures(CallbackInfo ci) {
        CustomLiquidTextures.INSTANCE.refresh((TextureAtlas) (Object) this);
    }
}