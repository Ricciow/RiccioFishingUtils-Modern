package cloud.glitchdev.rfu.data.other.data

import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.constants.fishing.LiquidTypes
import cloud.glitchdev.rfu.constants.chat.PartyTypes
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.party.PartyRequirementsManager

data class PartyPresetsEntry(
    var lastPartyState: PartyPresetData? = null,
    var presets: MutableMap<String, PartyPresetData> = mutableMapOf(),
    var version: Int = 0,
) : Entry

data class PartyPresetData(
    var name: String = "",
    var title: String = "",
    var description: String = "",
    var island: FishingIslands = FishingIslands.ISLE,
    var liquid: LiquidTypes = LiquidTypes.LAVA,
    var fishingType: PartyTypes = PartyTypes.REGULAR,
    var level: Int = 0,
    var maxPlayers: Int = 6,
    var hasKiller: Boolean = false,
    var enderman9: Boolean = false,
    var brainFood: Boolean = false,
    var bloodshot: Boolean = false,
    var overflow: Long? = null,
    var witherImpact: Boolean = false,
    var bobbinTimeLevel: Int = 0
) {
    fun applyTo(party: FishingParty) {
        party.removeLootingRequirement()
        party.title = title
        party.description = description
        party.island = island
        party.liquid = liquid
        party.fishingType = fishingType
        party.level = level
        party.overflow = overflow
        party.players.max = if (maxPlayers > 0) maxPlayers else 6
        party.setRequisite("has_killer", "Has Killer", hasKiller)
        party.setRequisite("enderman_9", "Enderman 9", enderman9 && PartyRequirementsManager.hasEnderman9())
        party.setRequisite("brain_food", "Brain Food", brainFood && PartyRequirementsManager.hasBrainFood())
        party.setRequisite("bloodshot", "Bloodshot", bloodshot && PartyRequirementsManager.hasBloodshotBelt())
        party.setRequisite("wither_impact", "Wither Impact", witherImpact && PartyRequirementsManager.hasWitherImpact())
        val bobbinTimeEnabled = bobbinTimeLevel in 3..5 && PartyRequirementsManager.hasBobbinTime(bobbinTimeLevel)
        party.setRequisite("bobbin_time", "Bobbin' Time", bobbinTimeEnabled, bobbinTimeLevel.takeIf { bobbinTimeEnabled })
    }

    fun copyFrom(party: FishingParty, presetName: String = name) {
        name = presetName
        title = party.title
        description = party.description
        island = party.island
        liquid = party.liquid
        fishingType = party.fishingType
        level = party.level
        overflow = party.overflow
        maxPlayers = party.players.max
        hasKiller = party.getRequisite("has_killer", "Has Killer").value
        enderman9 = party.getRequisite("enderman_9", "Enderman 9").value
        brainFood = party.getRequisite("brain_food", "Brain Food").value
        bloodshot = party.getRequisite("bloodshot", "Bloodshot").value
        witherImpact = party.getRequisite("wither_impact", "Wither Impact").value
        val bobbinTime = party.getRequisite("bobbin_time", "Bobbin' Time")
        bobbinTimeLevel = if (bobbinTime.value) bobbinTime.minimumLevel ?: 0 else 0
    }
}
