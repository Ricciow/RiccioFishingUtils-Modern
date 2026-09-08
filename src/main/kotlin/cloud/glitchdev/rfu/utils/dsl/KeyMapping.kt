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
        InputConstants.Type.KEYSYM.getOrCreate(this)
    }
}

fun InputConstants.Key.toConfigCode(): Int {
    return when (type) {
        InputConstants.Type.MOUSE -> -(100 + value)
        InputConstants.Type.KEYSYM -> value
        InputConstants.Type.SCANCODE -> value
    }
}