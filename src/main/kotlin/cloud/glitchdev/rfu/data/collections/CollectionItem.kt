package cloud.glitchdev.rfu.data.collections

enum class CollectionItem(
    val displayName: String,
    val items: List<Pair<String, Int>> = listOf(displayName to 1)
) {
    LILY_PAD("Lily Pad"),
    PRISMARINE_SHARD("Prismarine Shard"),
    INK_SAC("Ink Sac", listOf("Ink Sac" to 1, "Enchanted Ink Sac" to 80)),
    RAW_COD("Raw Cod"),
    PUFFERFISH("Pufferfish"),
    TROPICAL_FISH("Tropical Fish"),
    RAW_SALMON("Raw Salmon"),
    MAGMAFISH("Magmafish"),
    PRISMARINE_CRYSTALS("Prismarine Crystals"),
    CLAY_BALL("Clay Ball"),
    SPONGE("Sponge");

    val collectionRegex = Regex("$displayName: ([\\d,]+)")
    val sackRegexes: List<Pair<Regex, Int>> = items.map { (name, multiplier) ->
        Regex("""\+(\d+) $name""") to multiplier
    }

    companion object {
        fun fromId(id: String): CollectionItem? = entries.find { it.name == id }
    }
}
