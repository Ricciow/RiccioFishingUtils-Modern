package cloud.glitchdev.rfu.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import cloud.glitchdev.rfu.feature.fishing.FishingKeybindsHandler;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void onSet(InputConstants.Key key, boolean state, CallbackInfo ci) {
        if (FishingKeybindsHandler.INSTANCE.handleKeySet(key, state)) {
            ci.cancel();
        }
    }

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void onClick(InputConstants.Key key, CallbackInfo ci) {
        if (FishingKeybindsHandler.INSTANCE.handleKeyClick(key)) {
            ci.cancel();
        }
    }
}
