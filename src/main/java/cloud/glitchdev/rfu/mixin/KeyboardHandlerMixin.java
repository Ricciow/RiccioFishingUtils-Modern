package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.events.managers.KeybindEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void rfu$onKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (KeybindEvents.INSTANCE.handleKeyEvent(event.key(), action, event.modifiers())) {
            ci.cancel();
        }
    }
}
