package cloud.glitchdev.rfu.constants

enum class RareDrops(val dropName : String, override val rarity: Rarity, val relatedScNames : List<String> = listOf(), val overrideRegex : String? = null) : IRareDrop {
    //Drops
    BURNT_TEXTS("Burnt Texts", Rarity.LEGENDARY, listOf("Ragnarok")),
    RADIOACTIVE_VIAL("Radioactive Vial", Rarity.MYTHIC, listOf("Lord Jawbus")),
    TIKI_MASK("Tiki Mask", Rarity.LEGENDARY, listOf("Wiki Tiki")),
    TITANOBOA_SHED("Titanoboa Shed", Rarity.LEGENDARY, listOf("Titanoboa")),
    LUCKY_CLOVER_CORE("Lucky Clover Core", Rarity.EPIC, listOf("Carrot King")),
    FLASH_BOOK("Enchanted Book (Flash I)", Rarity.COMMON, listOf("Thunder"), """Enchanted Book \(Flash (?:1|I)\)""");

    override val displayName: String get() = dropName

    val relatedScs: List<SeaCreatures>
        get() = relatedScNames.mapNotNull { SeaCreatures.get(it) }

    override fun toString(): String {
        return dropName
    }

    companion object {
        fun getRelatedDrop(name : String) : RareDrops? {
            return RareDrops.entries.find { it.dropName == name || it.overrideRegex?.toRegex()?.matches(name) ?: false}
        }
    }
}
