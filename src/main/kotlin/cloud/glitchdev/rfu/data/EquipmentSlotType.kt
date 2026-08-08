package cloud.glitchdev.rfu.data

enum class EquipmentSlotType(val displayName: String, val containerSlot: Int) {
    NECKLACE("Necklace", 10),
    CLOAK("Cloak", 19),
    BELT("Belt", 28),
    GLOVES("Gloves", 37);

    companion object {
        fun fromContainerSlot(slot: Int): EquipmentSlotType? = entries.find { it.containerSlot == slot }
    }
}
