package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.constants.LiquidTypes.*
import cloud.glitchdev.rfu.constants.SeaCreatureCategory.*
import cloud.glitchdev.rfu.model.data.DataOption
import com.google.gson.annotations.SerializedName

enum class SeaCreatures(
    val scName: String,
    val liquidType: LiquidTypes,
    val category: SeaCreatureCategory,
    val special: Boolean = false
) {
    //Any water source
    @SerializedName("Sea Walker")
    SEA_WALKER("Sea Walker", WATER, GENERAL_WATER),
    @SerializedName("Squid")
    SQUID("Squid", WATER, GENERAL_WATER),
    @SerializedName("Night Squid")
    NIGHT_SQUID("Night Squid", WATER, GENERAL_WATER),
    @SerializedName("Sea Guardian")
    SEA_GUARDIAN("Sea Guardian", WATER, GENERAL_WATER),
    @SerializedName("Sea Witch")
    SEA_WITCH("Sea Witch", WATER, GENERAL_WATER),
    @SerializedName("Sea Archer")
    SEA_ARCHER("Sea Archer", WATER, GENERAL_WATER),
    @SerializedName("Rider of the Deep")
    RIDER_OF_THE_DEEP("Rider of the Deep", WATER, GENERAL_WATER),
    @SerializedName("Catfish")
    CATFISH("Catfish", WATER, GENERAL_WATER),
    @SerializedName("Carrot King")
    CARROT_KING("Carrot King", WATER, GENERAL_WATER, true),
    @SerializedName("Agarimoo")
    AGARIMOO("Agarimoo", WATER, GENERAL_WATER),
    @SerializedName("Sea Leech")
    SEA_LEECH("Sea Leech", WATER, GENERAL_WATER),
    @SerializedName("Guardian Defender")
    GUARDIAN_DEFENDER("Guardian Defender", WATER, GENERAL_WATER),
    @SerializedName("Deep Sea Protector")
    DEEP_SEA_PROTECTOR("Deep Sea Protector", WATER, GENERAL_WATER),
    @SerializedName("Water Hydra")
    WATER_HYDRA("Water Hydra", WATER, GENERAL_WATER, true),
    //Galatea
    @SerializedName("The Loch Emperor")
    SEA_EMPEROR("The Loch Emperor", WATER, GALATEA, true),
    @SerializedName("Wetwing")
    WETWING("Wetwing", WATER, GALATEA),
    @SerializedName("Tadgang")
    TADGANG("Tadgang", WATER, GALATEA),
    @SerializedName("Ent")
    ENT("Ent", WATER, GALATEA),
    @SerializedName("Stridersurfer")
    STRIDERSURFER("Stridersurfer", LAVA, GALATEA),
    //Water Hotspot
    @SerializedName("Frog Man")
    FROG_MAN("Frog Man", WATER, HOTSPOT_WATER),
    @SerializedName("Snapping Turtle")
    SNAPPING_TURTLE("Snapping Turtle", WATER, HOTSPOT_WATER),
    @SerializedName("Blue Ringed Octopus")
    BLUE_RINGED_OCTOPUS("Blue Ringed Octopus", WATER, HOTSPOT_WATER, true),
    @SerializedName("Wiki Tiki")
    WIKI_TIKI("Wiki Tiki", WATER, HOTSPOT_WATER, true),
    //Oasis
    @SerializedName("Oasis Rabbit")
    OASIS_RABBIT("Oasis Rabbit", WATER, OASIS),
    @SerializedName("Oasis Sheep")
    OASIS_SHEEP("Oasis Sheep", WATER, OASIS),
    //Crystal Hollows
    @SerializedName("Water Worm")
    WATER_WORM("Water Worm", WATER, HOLLOWS),
    @SerializedName("Poisoned Water Worm")
    POISONED_WATER_WORM("Poisoned Water Worm", WATER, HOLLOWS),
    @SerializedName("Abyssal Miner")
    ABYSSAL_MINER("Abyssal Miner", WATER, HOLLOWS, true),
    @SerializedName("Flaming Worm")
    FLAMING_WORM("Flaming Worm", LAVA, HOLLOWS),
    @SerializedName("Lava Blaze")
    LAVA_BLAZE("Lava Blaze", LAVA, HOLLOWS),
    @SerializedName("Lava Pigman")
    LAVA_PIGMAN("Lava Pigman", LAVA, HOLLOWS),
    //Dwarven
    @SerializedName("Mithril Grubber")
    NORMAL_GRUBBER("Mithril Grubber", WATER, DWARVEN),
    @SerializedName("Medium Mithril Grubber")
    MEDIUM_GRUBBER("Medium Mithril Grubber", WATER, DWARVEN),
    @SerializedName("Large Mithril Grubber")
    LARGE_GRUBBER("Large Mithril Grubber", WATER, DWARVEN),
    @SerializedName("Bloated Mithril Grubber")
    BLOATED_GRUBBER("Bloated Mithril Grubber", WATER, DWARVEN),
    //Bayou
    @SerializedName("Dumpster Diver")
    DUMPSTER_DIVER("Dumpster Diver", WATER, BAYOU),
    @SerializedName("Trash Gobbler")
    TRASH_GOBBLER("Trash Gobbler", WATER, BAYOU),
    @SerializedName("Banshee")
    BANSHEE("Banshee", WATER, BAYOU),
    @SerializedName("Bayou Sludge")
    BAYOU_SLUDGE("Bayou Sludge", WATER, BAYOU),
    @SerializedName("Alligator")
    ALLIGATOR("Alligator", WATER, BAYOU, true),
    @SerializedName("Titanoboa")
    TITANOBOA("Titanoboa", WATER, BAYOU, true),
    //Spooky
    @SerializedName("Scarecrow")
    SCARECROW("Scarecrow", WATER, SPOOKY),
    @SerializedName("Nightmare")
    NIGHTMARE("Nightmare", WATER, SPOOKY),
    @SerializedName("Werewolf")
    WEREWOLF("Werewolf", WATER, SPOOKY),
    @SerializedName("Phantom Fisher")
    PHANTOM_FISHER("Phantom Fisher", WATER, SPOOKY, true),
    @SerializedName("Grim Reaper")
    GRIM_REAPER("Grim Reaper", WATER, SPOOKY, true),
    //Winter
    @SerializedName("Frozen Steve")
    FROZEN_STEVE("Frozen Steve", WATER, WINTER),
    @SerializedName("Frosty")
    FROSTY("Frosty", WATER, WINTER),
    @SerializedName("Grinch")
    GRINCH("Grinch", WATER, WINTER),
    @SerializedName("Yeti")
    YETI("Yeti", WATER, WINTER, true),
    @SerializedName("Nutcracker")
    NUTCRACKER("Nutcracker", WATER, WINTER),
    @SerializedName("Reindrake")
    REINDRAKE("Reindrake", WATER, WINTER, true),
    //Festival
    @SerializedName("Nurse Shark")
    NURSE_SHARK("Nurse Shark", WATER, SHARK),
    @SerializedName("Blue Shark")
    BLUE_SHARK("Blue Shark", WATER, SHARK),
    @SerializedName("Tiger Shark")
    TIGER_SHARK("Tiger Shark", WATER, SHARK),
    @SerializedName("Great White Shark")
    GREAT_WHITE_SHARK("Great White Shark", WATER, SHARK, true),
    //Isle
    @SerializedName("Magma Slug")
    MAGMA_SLUG("Magma Slug", LAVA, ISLE),
    @SerializedName("Moogma")
    MOOGMA("Moogma", LAVA, ISLE),
    @SerializedName("Lava Leech")
    LAVA_LEECH("Lava Leech", LAVA, ISLE),
    @SerializedName("Pyroclastic Worm")
    PYROCLASTIC_WORM("Pyroclastic Worm", LAVA, ISLE),
    @SerializedName("Fire Eel")
    FIRE_EEL("Fire Eel", LAVA, ISLE),
    @SerializedName("Taurus")
    TAURUS("Taurus", LAVA, ISLE),
    @SerializedName("Plhlegblast")
    PLHLEGBLAST("Plhlegblast", LAVA, ISLE, true),
    @SerializedName("Thunder")
    THUNDER("Thunder", LAVA, ISLE, true),
    @SerializedName("Lord Jawbus")
    JAWBUS("Lord Jawbus", LAVA, ISLE, true),
    //Lava Hotspot
    @SerializedName("Fried Chicken")
    FRIED_CHICKEN("Fried Chicken", LAVA, HOTSPOT_LAVA),
    @SerializedName("Fireproof Witch")
    FIREPROOF_WITCH("Fireproof Witch", LAVA, HOTSPOT_LAVA),
    @SerializedName("Fiery Scutter")
    FIERY_SCUTTER("Fiery Scutter", LAVA, HOTSPOT_LAVA, true),
    @SerializedName("Ragnarock")
    RAGNAROCK("Ragnarock", LAVA, HOTSPOT_LAVA, true);

    fun toDataOption() : DataOption {
        return DataOption(this, this.scName)
    }

    companion object {
        fun toDataOptions(
            liquidType: LiquidTypes,
            island: FishingIslands,
            partyType: PartyTypes
        ): ArrayList<DataOption> {
            if (partyType.noMobs) return arrayListOf()

            val seaCreatures = SeaCreatures.entries.filter { sc ->
                if (sc.liquidType != liquidType) return@filter false
                if (!sc.category.islands.contains(island)) return@filter false
                if (!sc.category.partyTypes.contains(partyType)) return@filter false

                return@filter true
            }
                .sortedWith(
                    compareByDescending<SeaCreatures> { it.special }
                        .thenBy { it.category.islands.size }
                )

            return seaCreatures.map { sc ->
                sc.toDataOption()
            } as ArrayList<DataOption>
        }
    }
}