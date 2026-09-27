package cloud.glitchdev.rfu.data.other.data

data class FrogcoinBlessingsEntry(
    val activeBlessings: MutableMap<String, Long> = mutableMapOf()
) : Entry
