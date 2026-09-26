package cloud.glitchdev.rfu.constants.ui

enum class VisiblePlayerEquipment(val displayName: String) {
    NAME_TAG("Name Tag"),
    PLAYER_MODEL("Player Model"),
    HELMET("Helmet"),
    CHESTPLATE("Chestplate"),
    LEGGINGS("Leggings"),
    BOOTS("Boots"),
    HELD_ITEMS("Items in hand");

    override fun toString(): String = displayName
}