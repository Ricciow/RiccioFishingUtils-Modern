package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component

object TextUtils {
    fun rfuLiteral(string: String, textStyle: TextStyle) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§b§l] $textStyle$string")
    }

    fun rfuLiteral(string: String, textColor: TextColor = TextColor.WHITE) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§b§l] $textColor$string")
    }

    fun rfuLiteral(string: String, textColor: TextColor = TextColor.WHITE, textEffect : TextEffects) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§b§l] $textColor$textEffect$string")
    }

    fun rfupfLiteral(string: String, textStyle: TextStyle) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§9§lPF§b§l] $textStyle$string")
    }

    fun rfupfLiteral(string: String, textColor: TextColor = TextColor.WHITE) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§9§lPF§b§l] $textColor$string")
    }
}