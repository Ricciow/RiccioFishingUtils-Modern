package cloud.glitchdev.rfu.data.collections

enum class CollectionItem(
    val displayName: String
) {
    LILY_PAD("Lily Pad"),
    PRISMARINE_SHARD("Prismarine Shard"),
    INK_SAC("Ink Sac"),
    RAW_COD("Raw Cod"),
    PUFFERFISH("Pufferfish"),
    TROPICAL_FISH("Tropical Fish"),
    RAW_SALMON("Raw Salmon"),
    MAGMAFISH("Magmafish"),
    PRISMARINE_CRYSTALS("Prismarine Crystals"),
    CLAY_BALL("Clay Ball"),
    SPONGE("Sponge");

    val collectionRegex = Regex("$displayName: ([\\d,]+)")
    val sackRegex = Regex("""\+(\d+) $displayName""")

    companion object {
        fun fromId(id: String): CollectionItem? = entries.find { it.name == id }
    }
}
