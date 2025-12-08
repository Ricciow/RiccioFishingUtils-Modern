package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.gui.components.dropdown.DropdownOption

enum class FishingIslands(val island: String) {
    ISLE("Crimson Isle"),
    HOLLOWS("Crystal Hollows"),
    BAYOU("Backwater Bayou"),
    PARK("Park"),
    GALATEA("Galatea"),
    DESERT("The Farming Islands"),
    SPIDER("Spider's Den");

    companion object {
        fun toDropdownOptions() : ArrayList<DropdownOption> {
            return entries.map { island ->
                DropdownOption(island, island.island)
            } as ArrayList<DropdownOption>
        }
    }
}