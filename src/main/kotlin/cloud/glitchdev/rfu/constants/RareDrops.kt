package cloud.glitchdev.rfu.constants

enum class RareDrops(val dropName : String, val relatedScs : List<SeaCreatures> = listOf(), val overrideRegex : String? = null, val isDye : Boolean = false) {
    //Drops
    BURNT_TEXTS("Burnt Texts", listOf(SeaCreatures.RAGNAROK)),
    RADIOACTIVE_VIAL("Radioactive Vial", listOf(SeaCreatures.JAWBUS)),
    TIKI_MASK("Tiki Mask", listOf(SeaCreatures.WIKI_TIKI)),
    TITANOBOA_SHED("Titanoboa Shed", listOf(SeaCreatures.TITANOBOA)),
    FLASH_BOOK("Enchanted Book (Flash I)", listOf(SeaCreatures.THUNDER), """Enchanted Book \(Flash (?:1|I)\)"""),
    //Dyes
    CARMINE_DYE("Carmine Dye", SeaCreatures.entries.filter { it.liquidType == LiquidTypes.LAVA }, null, true),
    AQUAMARINE_DYE("Aquamarine Dye", SeaCreatures.entries.filter { it.liquidType == LiquidTypes.WATER } , null, true),
    ICEBERG_DYE("Iceberg Dye", SeaCreatures.entries.filter { it.category == SeaCreatureCategory.WINTER }, null, true);

    override fun toString(): String {
        return dropName
    }

    companion object {
        fun getRelatedDrop(name : String) : RareDrops? {
            return RareDrops.entries.find { it.dropName == name || it.overrideRegex?.toRegex()?.matches(name) ?: false}
        }
    }
}