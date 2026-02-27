package cloud.glitchdev.rfu.constants

enum class RareDrops(val dropName : String, val relatedScs : List<SeaCreatures> = listOf(), val overrideRegex : String? = null) {
    //Drops
    BURNT_TEXTS("Burnt Texts", listOf(SeaCreatures.RAGNAROK)),
    RADIOACTIVE_VIAL("Radioactive Vial", listOf(SeaCreatures.JAWBUS)),
    TIKI_MASK("Tiki Mask", listOf(SeaCreatures.WIKI_TIKI)),
    TITANOBOA_SHED("Titanoboa Shed", listOf(SeaCreatures.TITANOBOA)),
    FLASH_BOOK("Enchanted Book (Flash I)", listOf(SeaCreatures.THUNDER), """Enchanted Book \(Flash (?:1|I)\)""");

    override fun toString(): String {
        return dropName
    }

    companion object {
        fun getRelatedDrop(name : String) : RareDrops? {
            return RareDrops.entries.find { it.dropName == name || it.overrideRegex?.toRegex()?.matches(name) ?: false}
        }
    }
}