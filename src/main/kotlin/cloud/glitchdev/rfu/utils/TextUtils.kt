package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.constants.text.TextStyle
import net.minecraft.text.Text

object TextUtils {
    fun rfuLiteral(string: String, textStyle: TextStyle) : Text {
        return Text.literal("§b§l[§f§lRFU§b§l] $textStyle$string")
    }
}