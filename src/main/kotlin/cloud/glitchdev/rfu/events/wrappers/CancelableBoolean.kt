package cloud.glitchdev.rfu.events.wrappers

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

class CancelableBoolean(
    private val cir : CallbackInfoReturnable<Boolean>,
    private val cancelValue : Boolean
) {
    fun cancel() {
        cir.returnValue = cancelValue
    }
}