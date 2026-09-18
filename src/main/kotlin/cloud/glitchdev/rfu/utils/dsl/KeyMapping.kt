package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.mixin.KeyMappingAccessor
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping

val KeyMapping.rfuKey: InputConstants.Key
    get() = (this as KeyMappingAccessor).`rfu$GetKey`()

fun Int.toInputKey(): InputConstants.Key? {
    if (this == 0 || this == -1) return null
    return if (this < 0) {
        val button = if (this == -99) 2 else -this - 100
        if (button < 0) return null
        InputConstants.Type.MOUSE.getOrCreate(button)
    } else {
        //~ if <26.3 'KEYBOARD' -> 'KEYSYM' {
        InputConstants.Type.KEYBOARD.getOrCreate(this)
        //~}
    }
}

fun InputConstants.Key.toConfigCode(): Int {
    if (type == InputConstants.Type.MOUSE) {
        return -(100 + value)
    }
    return value
}