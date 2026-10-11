package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.constants.text.TextStyle
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.Font
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Component

object TextUtils {
    @JvmStatic
    fun patchSkyOceanTextWidth(font: Font, text: String, width: Int): Int {
        if (!OtherSettings.patchSkyOceanTextWidth || !FabricLoader.getInstance().isModLoaded("skyocean")) return width
        return font.width(Language.getInstance().getVisualOrder(FormattedText.of(text)))
    }

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

    fun rfupfLiteral(string: String, textColor: TextColor = TextColor.WHITE, textEffect : TextEffects) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§9§lPF§b§l] $textColor$textEffect$string")
    }

    fun debugLiteral(string: String, textStyle: TextStyle) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§1§lDEBUG§b§l] $textStyle$string")
    }

    fun debugLiteral(string: String, textColor: TextColor = TextColor.WHITE) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§1§lDEBUG§b§l] $textColor$string")
    }

    fun debugLiteral(string: String, textColor: TextColor = TextColor.WHITE, textEffect : TextEffects) : MutableComponent {
        return Component.literal("§b§l[§f§lRFU§1§lDEBUG§b§l] $textColor$textEffect$string")
    }

    fun backendAcceptMessage() : MutableComponent {
        return rfuLiteral(
            "Must accept the backend features to use this feature!",
            TextStyle(TextColor.LIGHT_RED, TextEffects.UNDERLINE)
        ).append(
            Component.literal("\n\n${TextColor.LIGHT_RED}/rfu -> Backend Settings -> Connect to Backend")
        )
    }
}