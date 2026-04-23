package cloud.glitchdev.rfu.constants

import kotlin.collections.listOf

enum class SeaCreatureCategory(val islands: List<FishingIslands>, val partyTypes: List<PartyTypes>) {
    GENERAL_WATER(
        listOf(
            FishingIslands.HUB,
            FishingIslands.DESERT,
            FishingIslands.SPIDER,
            FishingIslands.HOLLOWS,
            FishingIslands.BAYOU,
            FishingIslands.PARK
        ),
        listOf(PartyTypes.REGULAR, PartyTypes.HOTSPOT, PartyTypes.BARN)
    ),
    HOTSPOT_WATER(
        listOf(FishingIslands.BAYOU, FishingIslands.HUB, FishingIslands.SPIDER, FishingIslands.JERRY),
        listOf(PartyTypes.HOTSPOT)
    ),
    HOTSPOT_LAVA(
        listOf(FishingIslands.ISLE),
        listOf(PartyTypes.HOTSPOT)
    ),
    OASIS(
        listOf(FishingIslands.DESERT),
        listOf(PartyTypes.REGULAR, PartyTypes.BARN)
    ),
    HOLLOWS(
        listOf(FishingIslands.HOLLOWS),
        listOf(PartyTypes.REGULAR, PartyTypes.BARN)
    ),
    DWARVEN(
        listOf(FishingIslands.DWARVEN),
        listOf(PartyTypes.REGULAR, PartyTypes.BARN)
    ),
    SPOOKY(
        listOf(
            FishingIslands.HUB,
            FishingIslands.DESERT,
            FishingIslands.GALATEA,
            FishingIslands.SPIDER,
            FishingIslands.HOLLOWS,
            FishingIslands.BAYOU,
            FishingIslands.PARK
        ),
        listOf(PartyTypes.REGULAR, PartyTypes.HOTSPOT, PartyTypes.BARN)
    ),
    WINTER(
        listOf(FishingIslands.JERRY),
        listOf(PartyTypes.REGULAR, PartyTypes.BARN)
    ),
    SHARK(
        listOf(
            FishingIslands.HUB,
            FishingIslands.DESERT,
            FishingIslands.GALATEA,
            FishingIslands.SPIDER,
            FishingIslands.HOLLOWS,
            FishingIslands.BAYOU,
            FishingIslands.PARK
        ),
        listOf(PartyTypes.REGULAR, PartyTypes.HOTSPOT, PartyTypes.BARN)
    ),
    ISLE(
        listOf(FishingIslands.ISLE),
        listOf(PartyTypes.REGULAR, PartyTypes.HOTSPOT, PartyTypes.BARN)
    ),
    GALATEA(
        listOf(FishingIslands.GALATEA),
        listOf(PartyTypes.REGULAR, PartyTypes.BARN)
    ),
    BAYOU(
        listOf(FishingIslands.BAYOU),
        listOf(PartyTypes.REGULAR, PartyTypes.HOTSPOT, PartyTypes.BARN)
    );

    val displayName: String
        get() = when (this) {
            GENERAL_WATER -> "General Water"
            HOTSPOT_WATER -> "Hotspot Water"
            HOTSPOT_LAVA -> "Hotspot Lava"
            SPOOKY -> "Spooky"
            SHARK -> "Shark"
            OASIS -> "Oasis (The Farming Islands)"
            else -> if (islands.size == 1) islands.first().island else name.lowercase().split("_").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        }
}