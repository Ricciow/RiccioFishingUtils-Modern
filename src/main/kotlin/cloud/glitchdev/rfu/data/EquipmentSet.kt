package cloud.glitchdev.rfu.data

data class EquipmentSet(
    val necklace: String = "",
    val cloak: String = "",
    val belt: String = "",
    val gloves: String = ""
) {
    val isEmpty: Boolean
        get() = necklace.isEmpty() && cloak.isEmpty() && belt.isEmpty() && gloves.isEmpty()

    fun hasChanged(other: EquipmentSet): Boolean {
        return necklace != other.necklace ||
                cloak != other.cloak ||
                belt != other.belt ||
                gloves != other.gloves
    }

    operator fun get(slot: EquipmentSlotType): String = when (slot) {
        EquipmentSlotType.NECKLACE -> necklace
        EquipmentSlotType.CLOAK -> cloak
        EquipmentSlotType.BELT -> belt
        EquipmentSlotType.GLOVES -> gloves
    }
}
