package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.KeybindEvents;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void rfu$onMouseButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        if (KeybindEvents.INSTANCE.handleMouseEvent(rawButtonInfo.button(), action, rawButtonInfo.modifiers())) {
            ci.cancel();
        }
    }
}
