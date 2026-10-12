package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.config.categories.DevSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.resources.SkinManager$1")
public abstract class SkinManagerMixin {
    @WrapOperation(
        method = "lambda$load$0",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"
        )
    )
    private static void rfu$suppressInvalidTextureSignature(Logger logger, String message, Object profileId, Operation<Void> original) {
        if (DevSettings.INSTANCE.getDevMode() && DevSettings.INSTANCE.getSuppressTextureSignatureLogs()
            && "Profile contained invalid signature for textures property (profile id: {})".equals(message)) {
            return;
        }
        original.call(logger, message, profileId);
    }
}