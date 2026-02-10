package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.User
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import java.util.regex.Pattern

fun String.removeFormatting() : String {
    return this.replace("§.".toRegex(), "")
}

fun String.removeRankTag() : String {
    return this.replace("""\[[A-Z]+\+*\]""".toRegex(), "").trim()
}

fun String.toExactRegex() : Regex {
    return """^$this$""".toRegex()
}

fun String.isUser() : Boolean {
    return User.isUser(this)
}

fun String.toInteractiveText(command: String, hoverText: Component = Component.literal("Click to execute: $command")): Component {
    val root = TextUtils.rfuLiteral("", TextStyle(TextColor.WHITE))

    val pattern = Pattern.compile("\\{(.*?)\\}")
    val matcher = pattern.matcher(this)

    var lastEnd = 0

    while (matcher.find()) {
        val textBefore = this.substring(lastEnd, matcher.start())
        if (textBefore.isNotEmpty()) {
            root.append(Component.literal(textBefore))
        }

        val clickableContent = matcher.group(1)

        val interactivePart = Component.literal(clickableContent).setStyle(
            Style.EMPTY
                .withClickEvent(ClickEvent.RunCommand(command))
                .withHoverEvent(HoverEvent.ShowText(hoverText))
        )

        root.append(interactivePart)
        lastEnd = matcher.end()
    }

    if (lastEnd < this.length) {
        root.append(Component.literal(this.substring(lastEnd)))
    }

    return root
}

fun String.parseHealthValue(): Int {
    var str = this
    var multiplier = 1.0

    if (str.endsWith("k", true)) {
        multiplier = 1000.0
        str = str.dropLast(1)
    } else if (str.endsWith("M", true)) {
        multiplier = 1_000_000.0
        str = str.dropLast(1)
    }

    str = if (multiplier > 1.0) {
        str.replace(",", ".")
    } else {
        str.replace(",", "")
    }

    return str.toDoubleOrNull()?.times(multiplier)?.toInt() ?: 0
}