package cloud.glitchdev.rfu.gui.components.textinput

internal class ColorCodeText(val text: String) {
    private val offsets = IntArray(text.length + 1)
    private val styles = Array(text.length) { "" }
    private val preview: String

    init {
        var color = ""
        var formats = ""
        var markerEnd = 0
        preview = buildString {
            for (index in text.indices) {
                offsets[index] = length
                if (isCodeAt(text, index, '&')) markerEnd = index + 2
                val style = if (index < markerEnd) "\u00a7r\u00a77" else "\u00a7r$color$formats"
                styles[index] = style
                if (index == 0 || styles[index - 1] != style) append(style)
                append(text[index])
                if (index + 1 == markerEnd) {
                    val code = text[index].lowercaseChar()
                    when (code) {
                        in '0'..'9', in 'a'..'f', 'r' -> {
                            color = if (code == 'r') "" else "\u00a7$code"
                            formats = ""
                        }
                        else -> if (!formats.contains(code)) formats += "\u00a7$code"
                    }
                }
            }
            offsets[text.length] = length
        }
    }

    fun render(start: Int = 0, end: Int = text.length): String =
        if (start == end) "" else styles[start] + preview.substring(offsets[start], offsets[end])

    companion object {
        private const val CODES = "0123456789abcdefklmnor"

        private fun isCodeAt(text: String, index: Int, prefix: Char): Boolean =
            text[index] == prefix && index + 1 < text.length && text[index + 1].lowercaseChar() in CODES

        private fun convert(text: String, from: Char, to: Char): String = buildString {
            for (index in text.indices) {
                append(if (isCodeAt(text, index, from)) to else text[index])
            }
        }

        fun toEditable(text: String): String = convert(text, '\u00a7', '&')
        fun toFormatted(text: String): String = convert(text, '&', '\u00a7')
    }
}
