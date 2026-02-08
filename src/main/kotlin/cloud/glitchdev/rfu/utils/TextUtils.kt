package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.constants.text.TextStyle
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component

object TextUtils {
    fun rfuLiteral(string: String, textStyle: TextStyle) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§b§l] $textStyle$string")
    }
}