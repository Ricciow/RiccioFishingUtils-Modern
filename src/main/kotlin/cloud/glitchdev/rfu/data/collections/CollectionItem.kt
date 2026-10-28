package cloud.glitchdev.rfu.data.collections

enum class CollectionItem(
    val displayName: String,
    val items: List<Pair<String, Int>> = listOf(displayName to 1)
) {
    LILY_PAD("Lily Pad", listOf("Enchanted Lily Pad" to 160, "Condensed Lily Pad" to 25600)),
    INK_SAC("Ink Sac", listOf("Ink Sac" to 1, "Enchanted Ink Sac" to 80)),
    RAW_COD("Raw Cod", listOf("Enchanted Raw Cod" to 160, "Enchanted Cooked Cod" to 25600)),
    PUFFERFISH("Pufferfish", listOf("Enchanted Pufferfish" to 160)),
    TROPICAL_FISH("Tropical Fish", listOf("Enchanted Tropical Fish" to 160)),
    RAW_SALMON("Raw Salmon", listOf("Enchanted Raw Salmon" to 160, "Enchanted Cooked Salmon" to 25600)),
    MAGMAFISH("Magmafish", listOf("Silver Magmafish" to 80, "Gold Magmafish" to 6400)),
    PRISMARINE_CRYSTALS("Prismarine Crystals", listOf("Enchanted Prismarine Crystals" to 160)),
    PRISMARINE_SHARD("Prismarine Shard", listOf("Enchanted Prismarine Shard" to 160)),
    CLAY_BALL("Clay Ball", listOf("Enchanted Clay" to 160, "Enchanted Clay Block" to 25600)),
    SPONGE("Sponge", listOf("Enchanted Sponge" to 40, "Enchanted Wet Sponge" to 1600));

    val collectionRegex = Regex("$displayName: ([\\d,]+)")
    val itemMultipliers = (listOf(displayName to 1) + items).toMap()
    val sackRegexes: List<Pair<Regex, Int>> = itemMultipliers.map { (name, multiplier) ->
        Regex("""([+-][\d,]+) ${Regex.escape(name)}(?=\s*(?:\(|\n|$))""") to multiplier
    }
}
