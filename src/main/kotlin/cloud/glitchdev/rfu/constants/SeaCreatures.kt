package cloud.glitchdev.rfu.constants
import cloud.glitchdev.rfu.constants.LiquidTypes.*
import cloud.glitchdev.rfu.constants.SeaCreatureCategory.*

enum class SeaCreatures(val scName : String, val liquidType: LiquidTypes, val category: SeaCreatureCategory) {
    //Any water source
    SEA_WALKER("Sea Walker", WATER, GENERAL_WATER),
    SQUID("Squid", WATER, GENERAL_WATER),
    NIGHT_SQUID("Night Squid", WATER, GENERAL_WATER),
    SEA_GUARDIAN("Sea Guardian", WATER, GENERAL_WATER),
    SEA_WITCH("Sea Witch", WATER, GENERAL_WATER),
    SEA_ARCHER("Sea Archer", WATER, GENERAL_WATER),
    RIDER_OF_THE_DEEP("Rider of the Deep", WATER, GENERAL_WATER),
    CATFISH("Catfish", WATER, GENERAL_WATER),
    CARROT_KING("Carrot King", WATER, GENERAL_WATER),
    AGARIMOO("Agarimoo", WATER, GENERAL_WATER),
    SEA_LEECH("Sea Leech", WATER, GENERAL_WATER),
    GUARDIAN_DEFENDER("Guardian Defender", WATER, GENERAL_WATER),
    DEEP_SEA_PROTECTOR("Deep Sea Protector", WATER, GENERAL_WATER),
    WATER_HYDRA("Water Hydra", WATER, GENERAL_WATER),
    //Galatea
    SEA_EMPEROR("The Loch Emperor", WATER, GALATEA),
    WETWING("Wetwing", WATER, GALATEA),
    TADGANG("Tadgang", WATER, GALATEA),
    ENT("Ent", WATER, GALATEA),
    STRIDERSURFER("Stridersurfer", LAVA, GALATEA),
    //Water Hotspot
    FROG_MAN("Frog Man", WATER, HOTSPOT_WATER),
    SNAPPING_TURTLE("Snapping Turtle", WATER, HOTSPOT_WATER),
    BLUE_RINGED_OCTOPUS("Blue Ringed Octopus", WATER, HOTSPOT_WATER),
    WIKI_TIKI("Wiki Tiki", WATER, HOTSPOT_WATER),
    //Oasis
    OASIS_RABBIT("Oasis Rabbit", WATER, OASIS),
    OASIS_SHEEP("Oasis Sheep", WATER, OASIS),
    //Crystal Hollows
    WATER_WORM("Water Worm", WATER, HOLLOWS),
    POISONED_WATER_WORM("Poisoned Water Worm", WATER, HOLLOWS),
    ABYSSAL_MINER("Abyssal Miner", WATER, HOLLOWS),
    FLAMING_WORM("Flaming Worm", LAVA, HOLLOWS),
    LAVA_BLAZE("Lava Blaze", LAVA, HOLLOWS),
    LAVA_PIGMAN("Lava Pigman", LAVA, HOLLOWS),
    //Dwarven
    NORMAL_GRUBBER("Mithril Grubber", WATER, DWARVEN),
    MEDIUM_GRUBBER("Medium Mithril Grubber", WATER, DWARVEN),
    LARGE_GRUBBER("Large Mithril Grubber", WATER, DWARVEN),
    BLOATED_GRUBBER("Bloated Mithril Grubber", WATER, DWARVEN),
    //Bayou
    DUMPSTER_DIVER("Dumpster Diver", WATER, BAYOU),
    TRASH_GOBBLER("Trash Gobbler", WATER, BAYOU),
    BANSHEE("Banshee", WATER, BAYOU),
    BAYOU_SLUDGE("Bayou Sludge", WATER, BAYOU),
    ALLIGATOR("Alligator", WATER, BAYOU),
    TITANOBOA("Titanoboa", WATER, BAYOU),
    //Spooky
    SCARECROW("Scarecrow", WATER, SPOOKY),
    NIGHTMARE("Nightmare", WATER, SPOOKY),
    WEREWOLF("Werewolf", WATER, SPOOKY),
    PHANTOM_FISHER("Phantom Fisher", WATER, SPOOKY),
    GRIM_REAPER("Grim Reaper", WATER, SPOOKY),
    //Winter
    FROZEN_STEVE("Frozen Steve", WATER, WINTER),
    FROSTY("Frosty", WATER, WINTER),
    GRINCH("Grinch", WATER, WINTER),
    YETI("Yeti", WATER, WINTER),
    NUTCRACKER("Nutcracker", WATER, WINTER),
    REINDRAKE("Reindrake", WATER, WINTER),
    //Festival
    NURSE_SHARK("Nurse Shark", WATER, SHARK),
    BLUE_SHARK("Blue Shark", WATER, SHARK),
    TIGER_SHARK("Tiger Shark", WATER, SHARK),
    GREAT_WHITE_SHARK("Great White Shark", WATER, SHARK),
    //Isle
    MAGMA_SLUG("Magma Slug", LAVA, ISLE),
    MOOGMA("Moogma", LAVA, ISLE),
    LAVA_LEECH("Lava Leech", LAVA, ISLE),
    PYROCLASTIC_WORM("Pyroclastic Worm", LAVA, ISLE),
    FIRE_EEL("Fire Eel", LAVA, ISLE),
    TAURUS("Taurus", LAVA, ISLE),
    PLHLEGBLAST("Plhlegblast", LAVA, ISLE),
    THUNDER("Thunder", LAVA, ISLE),
    JAWBUS("Lord Jawbus", LAVA, ISLE),
    //Lava Hotspot
    FRIED_CHICKEN("Fried Chicken", LAVA, HOTSPOT_LAVA),
    FIREPROOF_WITCH("Fireproof Witch", LAVA, HOTSPOT_LAVA),
    FIERY_SCUTTER("Fiery Scutter", LAVA, HOTSPOT_LAVA),
    RAGNAROCK("Ragnarock", LAVA, HOTSPOT_LAVA)
}