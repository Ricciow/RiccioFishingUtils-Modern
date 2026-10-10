package cloud.glitchdev.rfu.model.party

import cloud.glitchdev.rfu.constants.chat.PartyTypes
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.constants.fishing.LiquidTypes

enum class PartyTypeOption(val label: String, val fishingType: PartyTypes) {
    LAVA("Lava", PartyTypes.REGULAR),
    WATER("Water", PartyTypes.REGULAR),
    HOTSPOT("Hotspot", PartyTypes.HOTSPOT),
    TREASURE("Treasure", PartyTypes.TREASURE),
    TROPHY("Trophy", PartyTypes.TROPHY);

    fun liquidFor(island: FishingIslands): LiquidTypes? = when (this) {
        LAVA -> LiquidTypes.LAVA.takeIf { it in island.availableLiquids }
        WATER -> LiquidTypes.WATER.takeIf { it in island.availableLiquids }
        HOTSPOT -> when (island) {
            FishingIslands.ISLE -> LiquidTypes.LAVA
            FishingIslands.BAYOU, FishingIslands.PARK, FishingIslands.SPIDER,
            FishingIslands.JERRY, FishingIslands.HUB, FishingIslands.ATOLL,
            FishingIslands.TORRHUS -> LiquidTypes.WATER
            else -> null
        }
        TREASURE -> if (LiquidTypes.WATER in island.availableLiquids) LiquidTypes.WATER else LiquidTypes.LAVA
        TROPHY -> when (island) {
            FishingIslands.ISLE -> LiquidTypes.LAVA
            FishingIslands.ATOLL -> LiquidTypes.WATER
            else -> null
        }
    }

    companion object {
        fun from(fishingType: PartyTypes, liquid: LiquidTypes): PartyTypeOption = when (fishingType) {
            PartyTypes.HOTSPOT -> HOTSPOT
            PartyTypes.TREASURE -> TREASURE
            PartyTypes.TROPHY -> TROPHY
            else -> if (liquid == LiquidTypes.LAVA) LAVA else WATER
        }
    }
}
