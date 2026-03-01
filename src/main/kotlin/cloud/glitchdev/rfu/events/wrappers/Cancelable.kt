package cloud.glitchdev.rfu.events.wrappers

import cloud.glitchdev.rfu.utils.RFULogger
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

class Cancelable<T>(
    private val cir : CallbackInfoReturnable<T>,
    private val cancelValue : T? = null
) {
    fun cancel() {
        if(cancelValue != null) {
            cir.returnValue = cancelValue
        } else {
            RFULogger.warn("Attempted to call Cancelable.cancel() without a cancelValue.")
        }
    }

    fun cancel(value : T) {
        cir.returnValue = value
    }

    fun getValue() : T {
        return cir.returnValue
    }
}