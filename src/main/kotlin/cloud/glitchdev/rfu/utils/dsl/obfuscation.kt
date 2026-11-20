package cloud.glitchdev.rfu.utils.dsl

internal fun String.withoutObfuscation(): String = buildString(length) {
    for (char in this@withoutObfuscation) {
        val previous = lastOrNull()
        if ((previous == '\u00a7' || previous == '&') && char.lowercaseChar() == 'k') {
            deleteCharAt(lastIndex)
        } else {
            append(char)
        }
    }
}
