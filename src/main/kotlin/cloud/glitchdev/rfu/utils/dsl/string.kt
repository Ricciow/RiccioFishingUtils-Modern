package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.User
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
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

fun String.toInteractiveText(command: String, hoverText: Text = Text.literal("Click to execute: $command")): Text {
    val root = TextUtils.rfuLiteral("", TextStyle(TextColor.WHITE)) as MutableText

    val pattern = Pattern.compile("\\{(.*?)\\}")
    val matcher = pattern.matcher(this)

    var lastEnd = 0

    while (matcher.find()) {
        val textBefore = this.substring(lastEnd, matcher.start())
        if (textBefore.isNotEmpty()) {
            root.append(Text.literal(textBefore))
        }

        val clickableContent = matcher.group(1)

        val interactivePart = Text.literal(clickableContent).setStyle(
            Style.EMPTY
                .withClickEvent(ClickEvent.RunCommand(command))
                .withHoverEvent(HoverEvent.ShowText(hoverText))
        )

        root.append(interactivePart)
        lastEnd = matcher.end()
    }

    if (lastEnd < this.length) {
        root.append(Text.literal(this.substring(lastEnd)))
    }

    return root
}