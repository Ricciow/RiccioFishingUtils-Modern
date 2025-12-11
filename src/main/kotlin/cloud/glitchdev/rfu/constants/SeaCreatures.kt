package cloud.glitchdev.rfu.constants
import cloud.glitchdev.rfu.constants.LiquidTypes.*
import cloud.glitchdev.rfu.constants.SeaCreatureCategory.*
import cloud.glitchdev.rfu.model.data.DataOption

enum class SeaCreatures(val scName : String, val liquidType: LiquidTypes, val category: SeaCreatureCategory, val special : Boolean) {
    //Any water source
    SEA_WALKER("Sea Walker", WATER, GENERAL_WATER, false),
    SQUID("Squid", WATER, GENERAL_WATER, false),
    NIGHT_SQUID("Night Squid", WATER, GENERAL_WATER, false),
    SEA_GUARDIAN("Sea Guardian", WATER, GENERAL_WATER, false),
    SEA_WITCH("Sea Witch", WATER, GENERAL_WATER, false),
    SEA_ARCHER("Sea Archer", WATER, GENERAL_WATER, false),
    RIDER_OF_THE_DEEP("Rider of the Deep", WATER, GENERAL_WATER, false),
    CATFISH("Catfish", WATER, GENERAL_WATER, false),
    CARROT_KING("Carrot King", WATER, GENERAL_WATER, true),
    AGARIMOO("Agarimoo", WATER, GENERAL_WATER, false),
    SEA_LEECH("Sea Leech", WATER, GENERAL_WATER, false),
    GUARDIAN_DEFENDER("Guardian Defender", WATER, GENERAL_WATER, false),
    DEEP_SEA_PROTECTOR("Deep Sea Protector", WATER, GENERAL_WATER, false),
    WATER_HYDRA("Water Hydra", WATER, GENERAL_WATER, true),
    //Galatea
    SEA_EMPEROR("The Loch Emperor", WATER, GALATEA, true),
    WETWING("Wetwing", WATER, GALATEA, false),
    TADGANG("Tadgang", WATER, GALATEA, false),
    ENT("Ent", WATER, GALATEA, false),
    STRIDERSURFER("Stridersurfer", LAVA, GALATEA, false),
    //Water Hotspot
    FROG_MAN("Frog Man", WATER, HOTSPOT_WATER, false),
    SNAPPING_TURTLE("Snapping Turtle", WATER, HOTSPOT_WATER, false),
    BLUE_RINGED_OCTOPUS("Blue Ringed Octopus", WATER, HOTSPOT_WATER, true),
    WIKI_TIKI("Wiki Tiki", WATER, HOTSPOT_WATER, true),
    //Oasis
    OASIS_RABBIT("Oasis Rabbit", WATER, OASIS, false),
    OASIS_SHEEP("Oasis Sheep", WATER, OASIS, false),
    //Crystal Hollows
    WATER_WORM("Water Worm", WATER, HOLLOWS, false),
    POISONED_WATER_WORM("Poisoned Water Worm", WATER, HOLLOWS, false),
    ABYSSAL_MINER("Abyssal Miner", WATER, HOLLOWS, true),
    FLAMING_WORM("Flaming Worm", LAVA, HOLLOWS, false),
    LAVA_BLAZE("Lava Blaze", LAVA, HOLLOWS, false),
    LAVA_PIGMAN("Lava Pigman", LAVA, HOLLOWS, false),
    //Dwarven
    NORMAL_GRUBBER("Mithril Grubber", WATER, DWARVEN, false),
    MEDIUM_GRUBBER("Medium Mithril Grubber", WATER, DWARVEN, false),
    LARGE_GRUBBER("Large Mithril Grubber", WATER, DWARVEN, false),
    BLOATED_GRUBBER("Bloated Mithril Grubber", WATER, DWARVEN, false),
    //Bayou
    DUMPSTER_DIVER("Dumpster Diver", WATER, BAYOU, false),
    TRASH_GOBBLER("Trash Gobbler", WATER, BAYOU, false),
    BANSHEE("Banshee", WATER, BAYOU, false),
    BAYOU_SLUDGE("Bayou Sludge", WATER, BAYOU, false),
    ALLIGATOR("Alligator", WATER, BAYOU, true),
    TITANOBOA("Titanoboa", WATER, BAYOU, true),
    //Spooky
    SCARECROW("Scarecrow", WATER, SPOOKY, false),
    NIGHTMARE("Nightmare", WATER, SPOOKY, false),
    WEREWOLF("Werewolf", WATER, SPOOKY, false),
    PHANTOM_FISHER("Phantom Fisher", WATER, SPOOKY, true),
    GRIM_REAPER("Grim Reaper", WATER, SPOOKY, true),
    //Winter
    FROZEN_STEVE("Frozen Steve", WATER, WINTER, false),
    FROSTY("Frosty", WATER, WINTER, false),
    GRINCH("Grinch", WATER, WINTER, false),
    YETI("Yeti", WATER, WINTER, true),
    NUTCRACKER("Nutcracker", WATER, WINTER, false),
    REINDRAKE("Reindrake", WATER, WINTER, true),
    //Festival
    NURSE_SHARK("Nurse Shark", WATER, SHARK, false),
    BLUE_SHARK("Blue Shark", WATER, SHARK, false),
    TIGER_SHARK("Tiger Shark", WATER, SHARK, false),
    GREAT_WHITE_SHARK("Great White Shark", WATER, SHARK, true),
    //Isle
    MAGMA_SLUG("Magma Slug", LAVA, ISLE, false),
    MOOGMA("Moogma", LAVA, ISLE, false),
    LAVA_LEECH("Lava Leech", LAVA, ISLE, false),
    PYROCLASTIC_WORM("Pyroclastic Worm", LAVA, ISLE, false),
    FIRE_EEL("Fire Eel", LAVA, ISLE, false),
    TAURUS("Taurus", LAVA, ISLE, false),
    PLHLEGBLAST("Plhlegblast", LAVA, ISLE, true),
    THUNDER("Thunder", LAVA, ISLE, true),
    JAWBUS("Lord Jawbus", LAVA, ISLE, true),
    //Lava Hotspot
    FRIED_CHICKEN("Fried Chicken", LAVA, HOTSPOT_LAVA, false),
    FIREPROOF_WITCH("Fireproof Witch", LAVA, HOTSPOT_LAVA, false),
    FIERY_SCUTTER("Fiery Scutter", LAVA, HOTSPOT_LAVA, true),
    RAGNAROCK("Ragnarock", LAVA, HOTSPOT_LAVA, false);

    companion object {
        fun toDataOptions(liquidType: LiquidTypes, island: FishingIslands) : ArrayList<DataOption> {
            val seaCreatures = SeaCreatures.entries.filter { sc ->
                if(sc.liquidType != liquidType) return@filter false
                if(!sc.category.islands.contains(island)) return@filter false

                return@filter true
            }.sortedByDescending { it.special }

            return seaCreatures.map { sc ->
                DataOption(sc, sc.scName)
            } as ArrayList<DataOption>
        }
    }
}