package cloud.glitchdev.rfu.data.other.data

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.constants.fishing.LiquidTypes
import cloud.glitchdev.rfu.model.party.FishingParty

data class PartyPresetsEntry(
    var lastPartyState: PartyPresetData? = null,
    var presets: MutableMap<String, PartyPresetData> = mutableMapOf()
) : Entry

data class PartyPresetData(
    var name: String = "",
    var title: String = "",
    var description: String = "",
    var island: FishingIslands = FishingIslands.ISLE,
    var liquid: LiquidTypes = LiquidTypes.LAVA,
    var level: Int = 0,
    var maxPlayers: Int = 6,
    var hasKiller: Boolean = false,
    var enderman9: Boolean = false,
    var looting5: Boolean = false,
    var brainFood: Boolean = false,
    var bloodshot: Boolean = false
) {
    fun applyTo(party: FishingParty) {
        party.title = title
        party.description = description
        party.island = island
        party.liquid = liquid
        party.level = level
        party.players.max = if (maxPlayers > 0) maxPlayers else 6
        party.setRequisite("has_killer", "Has Killer", hasKiller)
        party.setRequisite("enderman_9", "Enderman 9", enderman9)
        party.setRequisite("looting_5", "Looting 5", looting5)
        party.setRequisite("brain_food", "Brain Food", brainFood)
        party.setRequisite("bloodshot", "Bloodshot", bloodshot)
    }

    fun copyFrom(party: FishingParty, presetName: String = name) {
        name = presetName
        title = party.title
        description = party.description
        island = party.island
        liquid = party.liquid
        level = party.level
        maxPlayers = party.players.max
        hasKiller = party.getRequisite("has_killer", "Has Killer").value
        enderman9 = party.getRequisite("enderman_9", "Enderman 9").value
        looting5 = party.getRequisite("looting_5", "Looting 5").value
        brainFood = party.getRequisite("brain_food", "Brain Food").value
        bloodshot = party.getRequisite("bloodshot", "Bloodshot").value
    }
}
