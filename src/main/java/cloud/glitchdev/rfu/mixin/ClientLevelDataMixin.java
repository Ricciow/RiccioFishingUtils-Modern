package cloud.glitchdev.rfu.mixin;

import cloud.glitchdev.rfu.utils.gui.HeartsUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.ClientLevelData.class)
public class ClientLevelDataMixin {
    @Inject(method = "isHardcore", at = @At("HEAD"), cancellable = true)
    private void rfu$isHardcore(CallbackInfoReturnable<Boolean> cir) {
        if (HeartsUtil.getForceHardcoreHearts()) {
            cir.setReturnValue(true);
        }
    }
}
