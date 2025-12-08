package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.gui.components.dropdown.DropdownOption

enum class PartyTypes(val type: String){
    REGULAR("Normal Fishing"),
    HOTSPOT("Hotspot Fishing"),
    BARN("Barn Fishing"),
    TROPHY("Trophy Fishing"),
    TREASURE("Treasure Fishing");

    companion object {
        fun toDropdownOptions() : ArrayList<DropdownOption> {
            return entries.map { party ->
                DropdownOption(party, party.type)
            } as ArrayList<DropdownOption>
        }
    }
}