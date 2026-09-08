package cloud.glitchdev.rfu.mixin.elementa;

import cloud.glitchdev.rfu.config.categories.OtherSettings;
import gg.essential.elementa.constraints.animation.AnimatingConstraints;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AnimatingConstraints.class, remap = false)
public abstract class AnimatingConstraintsMixin {
    @Shadow private Function0<Unit> completeAction;

    @Inject(
        method = "updateCompletion$Elementa",
        at = @At(value = "INVOKE", target = "Lkotlin/jvm/functions/Function0;invoke()Ljava/lang/Object;", shift = At.Shift.AFTER)
    )
    private void rfu$onCompleteActionInvoked(int dt, CallbackInfo ci) {
        if (OtherSettings.INSTANCE.getPatchElementaMemoryLeaks()) {
            this.completeAction = () -> Unit.INSTANCE;
        }
    }
}
