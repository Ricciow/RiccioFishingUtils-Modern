package cloud.glitchdev.rfu.constants

enum class RareDrops(val dropName : String, val rarity: Rarity, val relatedScs : List<SeaCreatures> = listOf(), val overrideRegex : String? = null) {
    //Drops
    BURNT_TEXTS("Burnt Texts", Rarity.LEGENDARY, listOf(SeaCreatures.RAGNAROK)),
    RADIOACTIVE_VIAL("Radioactive Vial", Rarity.MYTHIC, listOf(SeaCreatures.JAWBUS)),
    TIKI_MASK("Tiki Mask", Rarity.LEGENDARY,listOf(SeaCreatures.WIKI_TIKI)),
    TITANOBOA_SHED("Titanoboa Shed", Rarity.LEGENDARY,listOf(SeaCreatures.TITANOBOA)),
    LUCKY_CLOVER_CORE("Lucky Clover Core", Rarity.EPIC,listOf(SeaCreatures.CARROT_KING)),
    FLASH_BOOK("Enchanted Book (Flash I)", Rarity.COMMON,listOf(SeaCreatures.THUNDER), """Enchanted Book \(Flash (?:1|I)\)""");

    override fun toString(): String {
        return dropName
    }

    companion object {
        fun getRelatedDrop(name : String) : RareDrops? {
            return RareDrops.entries.find { it.dropName == name || it.overrideRegex?.toRegex()?.matches(name) ?: false}
        }
    }
}