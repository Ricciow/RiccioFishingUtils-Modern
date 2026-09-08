package cloud.glitchdev.rfu.constants.text

class TextStyle(val color: TextColor?, val effects: List<TextEffects> = listOf()) {
    constructor(color : TextColor, effect: TextEffects) : this(color, listOf(effect))
    constructor(effect: TextEffects) : this(null, listOf(effect))
    constructor() : this(null, listOf())

    override fun toString(): String {
        return "${color?:""}${effects.joinToString("")}"
    }
}