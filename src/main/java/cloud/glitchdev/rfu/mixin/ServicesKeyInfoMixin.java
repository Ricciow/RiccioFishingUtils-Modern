package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.config.categories.DevSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.properties.Property;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//~ if >=26.3 'com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo' -> 'com.mojang.authlib.services.MinecraftServicesKeyInfo' {
@Mixin(targets = "com.mojang.authlib.services.MinecraftServicesKeyInfo", remap = false)
//~}
public abstract class ServicesKeyInfoMixin {
    @WrapOperation(
            method = "validateProperty",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private void rfu$suppressTextureSignatureError(Logger logger, String message, Object property, Object error, Operation<Void> original) {
        if (DevSettings.INSTANCE.getDevMode() && DevSettings.INSTANCE.getSuppressTextureSignatureLogs()
                && ("Malformed signature encoding on property {}".equals(message)
                || "Failed to verify signature on property {}".equals(message))
                && property instanceof Property textureProperty && "textures".equals(textureProperty.name())) {
            return;
        }
        original.call(logger, message, property, error);
    }
}