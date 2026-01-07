package cloud.glitchdev.rfu.constants.text

class TextStyle(val color: TextColor, val effects: List<TextEffects> = listOf()) {
    constructor(color : TextColor, effect: TextEffects) : this(color, listOf(effect))

    override fun toString(): String {
        return "$color${effects.joinToString("")}"
    }
}