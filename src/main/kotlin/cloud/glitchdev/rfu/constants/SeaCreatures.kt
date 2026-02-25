package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.constants.LiquidTypes.*
import cloud.glitchdev.rfu.constants.SeaCreatureCategory.*
import cloud.glitchdev.rfu.model.data.DataOption
import com.google.gson.annotations.SerializedName

enum class SeaCreatures(
    val scName: String,
    val catchMessage : String,
    val liquidType: LiquidTypes,
    val category: SeaCreatureCategory,
    val special: Boolean = false
) {
    //Any water source
    @SerializedName("Sea Walker")
    SEA_WALKER("Sea Walker", "You caught a Sea Walker.",WATER, GENERAL_WATER),
    @SerializedName("Squid")
    SQUID("Squid", "A Squid appeared.",WATER, GENERAL_WATER),
    @SerializedName("Night Squid")
    NIGHT_SQUID("Night Squid", "Pitch darkness reveals a Night Squid.", WATER, GENERAL_WATER),
    @SerializedName("Sea Guardian")
    SEA_GUARDIAN("Sea Guardian", "You stumbled upon a Sea Guardian.", WATER, GENERAL_WATER),
    @SerializedName("Sea Witch")
    SEA_WITCH("Sea Witch", "It looks like you've disrupted the Sea Witch's brewing session. Watch out, she's furious!", WATER, GENERAL_WATER),
    @SerializedName("Sea Archer")
    SEA_ARCHER("Sea Archer", "You reeled in a Sea Archer.", WATER, GENERAL_WATER),
    @SerializedName("Rider of the Deep")
    RIDER_OF_THE_DEEP("Rider of the Deep", "The Rider of the Deep has emerged.", WATER, GENERAL_WATER),
    @SerializedName("Catfish")
    CATFISH("Catfish", "Huh? A Catfish!", WATER, GENERAL_WATER),
    @SerializedName("Carrot King")
    CARROT_KING("Carrot King", "Is this even a fish? It's the Carrot King!", WATER, GENERAL_WATER, true),
    @SerializedName("Agarimoo")
    AGARIMOO("Agarimoo", "Your Chumcap Bucket trembles, it's an Agarimoo.", WATER, GENERAL_WATER),
    @SerializedName("Sea Leech")
    SEA_LEECH("Sea Leech", "Gross! A Sea Leech!", WATER, GENERAL_WATER),
    @SerializedName("Guardian Defender")
    GUARDIAN_DEFENDER("Guardian Defender", "You've discovered a Guardian Defender of the sea.", WATER, GENERAL_WATER),
    @SerializedName("Deep Sea Protector")
    DEEP_SEA_PROTECTOR("Deep Sea Protector", "You have awoken the Deep Sea Protector, prepare for a battle!", WATER, GENERAL_WATER),
    @SerializedName("Water Hydra")
    WATER_HYDRA("Water Hydra", "The Water Hydra has come to test your strength.", WATER, GENERAL_WATER, true),
    //Galatea
    @SerializedName("The Loch Emperor")
    SEA_EMPEROR("The Loch Emperor", "The Loch Emperor arises from the depths.", WATER, GALATEA, true),
    @SerializedName("Wetwing")
    WETWING("Wetwing", "Look! A Wetwing emerges!", WATER, GALATEA),
    @SerializedName("Tadgang")
    TADGANG("Tadgang", "A gang of Liltads!", WATER, GALATEA),
    @SerializedName("Ent")
    ENT("Ent", "You've hooked an Ent, as ancient as the forest itself.", WATER, GALATEA),
    @SerializedName("Bogged")
    BOGGED("Bogged", "You've hooked a Bogged!", WATER, GALATEA),
    @SerializedName("Stridersurfer")
    STRIDERSURFER("Stridersurfer", "You caught a Stridersurfer.", LAVA, GALATEA),
    //Water Hotspot
    @SerializedName("Frog Man")
    FROG_MAN("Frog Man", "Is it a frog? Is it a man? Well, yes, sorta, IT'S FROG MAN!!!!!!", WATER, HOTSPOT_WATER),
    @SerializedName("Snapping Turtle")
    SNAPPING_TURTLE("Snapping Turtle", "A Snapping Turtle is coming your way, and it's ANGRY!", WATER, HOTSPOT_WATER),
    @SerializedName("Blue Ringed Octopus")
    BLUE_RINGED_OCTOPUS("Blue Ringed Octopus", "A garish set of tentacles arise. It's a Blue Ringed Octopus!", WATER, HOTSPOT_WATER, true),
    @SerializedName("Wiki Tiki")
    WIKI_TIKI("Wiki Tiki", "The water bubbles and froths. A massive form emerges- you have disturbed the Wiki Tiki! You shall pay the price.", WATER, HOTSPOT_WATER, true),
    //Oasis
    @SerializedName("Oasis Rabbit")
    OASIS_RABBIT("Oasis Rabbit","An Oasis Rabbit appears from the water.", WATER, OASIS),
    @SerializedName("Oasis Sheep")
    OASIS_SHEEP("Oasis Sheep", "An Oasis Sheep appears from the water.", WATER, OASIS),
    //Crystal Hollows
    @SerializedName("Water Worm")
    WATER_WORM("Water Worm", "A Water Worm surfaces!", WATER, HOLLOWS),
    @SerializedName("Poisoned Water Worm")
    POISONED_WATER_WORM("Poisoned Water Worm", "A Poisoned Water Worm surfaces!", WATER, HOLLOWS),
    @SerializedName("Abyssal Miner")
    ABYSSAL_MINER("Abyssal Miner", "An Abyssal Miner breaks out of the water!", WATER, HOLLOWS, true),
    @SerializedName("Flaming Worm")
    FLAMING_WORM("Flaming Worm", "A Flaming Worm surfaces from the depths!", LAVA, HOLLOWS),
    @SerializedName("Lava Blaze")
    LAVA_BLAZE("Lava Blaze", "A Lava Blaze has surfaced from the depths!", LAVA, HOLLOWS),
    @SerializedName("Lava Pigman")
    LAVA_PIGMAN("Lava Pigman", "A Lava Pigman arose from the depths!", LAVA, HOLLOWS),
    //Dwarven
    @SerializedName("Mithril Grubber")
    NORMAL_GRUBBER("Mithril Grubber", "A leech of the mines surfaces... you've caught a Mithril Grubber.", WATER, DWARVEN),
    @SerializedName("Medium Mithril Grubber")
    MEDIUM_GRUBBER("Medium Mithril Grubber", "A leech of the mines surfaces... you've caught a Medium Mithril Grubber.", WATER, DWARVEN),
    @SerializedName("Large Mithril Grubber")
    LARGE_GRUBBER("Large Mithril Grubber", "A leech of the mines surfaces... you've caught a Large Mithril Grubber.", WATER, DWARVEN),
    @SerializedName("Bloated Mithril Grubber")
    BLOATED_GRUBBER("Bloated Mithril Grubber", "A leech of the mines surfaces... you've caught a Bloated Mithril Grubber.", WATER, DWARVEN),
    //Bayou
    @SerializedName("Dumpster Diver")
    DUMPSTER_DIVER("Dumpster Diver", "A Dumpster Diver has emerged from the swamp!", WATER, BAYOU),
    @SerializedName("Trash Gobbler")
    TRASH_GOBBLER("Trash Gobbler", "The Trash Gobbler is hungry for you!", WATER, BAYOU),
    @SerializedName("Banshee")
    BANSHEE("Banshee", "The desolate wail of a Banshee breaks the silence.", WATER, BAYOU),
    @SerializedName("Bayou Sludge")
    BAYOU_SLUDGE("Bayou Sludge", "A swampy mass of slime emerges, the Bayou Sludge!", WATER, BAYOU),
    @SerializedName("Alligator")
    ALLIGATOR("Alligator", "A long snout breaks the surface of the water. It's an Alligator!", WATER, BAYOU, true),
    @SerializedName("Titanoboa")
    TITANOBOA("Titanoboa", "A massive Titanoboa surfaces. It's body stretches as far as the eye can see.", WATER, BAYOU, true),
    //Spooky
    @SerializedName("Scarecrow")
    SCARECROW("Scarecrow", "Phew! It's only a Scarecrow.", WATER, SPOOKY),
    @SerializedName("Nightmare")
    NIGHTMARE("Nightmare", "You hear trotting from beneath the waves, you caught a Nightmare.", WATER, SPOOKY),
    @SerializedName("Werewolf")
    WEREWOLF("Werewolf", "It must be a full moon, a Werewolf appears.", WATER, SPOOKY),
    @SerializedName("Phantom Fisher")
    PHANTOM_FISHER("Phantom Fisher", "The spirit of a long lost Phantom Fisher has come to haunt you.", WATER, SPOOKY, true),
    @SerializedName("Grim Reaper")
    GRIM_REAPER("Grim Reaper", "This can't be! The manifestation of death himself!", WATER, SPOOKY, true),
    //Winter
    @SerializedName("Frozen Steve")
    FROZEN_STEVE("Frozen Steve", "Frozen Steve fell into the pond long ago, never to resurface...until now!", WATER, WINTER),
    @SerializedName("Frosty")
    FROSTY("Frosty", "It's a snowman! He looks harmless.", WATER, WINTER),
    @SerializedName("Grinch")
    GRINCH("Grinch", "The Grinch stole Jerry's Gifts...get them back!", WATER, WINTER),
    @SerializedName("Yeti")
    YETI("Yeti", "What is this creature!?", WATER, WINTER, true),
    @SerializedName("Nutcracker")
    NUTCRACKER("Nutcracker", "You found a forgotten Nutcracker laying beneath the ice.", WATER, WINTER),
    @SerializedName("Reindrake")
    REINDRAKE("Reindrake", "A Reindrake forms from the depths.", WATER, WINTER, true),
    //Festival
    @SerializedName("Nurse Shark")
    NURSE_SHARK("Nurse Shark", "A tiny fin emerges from the water, you've caught a Nurse Shark.", WATER, SHARK),
    @SerializedName("Blue Shark")
    BLUE_SHARK("Blue Shark", "You spot a fin as blue as the water it came from, it's a Blue Shark.", WATER, SHARK),
    @SerializedName("Tiger Shark")
    TIGER_SHARK("Tiger Shark", "A striped beast bounds from the depths, the wild Tiger Shark!", WATER, SHARK),
    @SerializedName("Great White Shark")
    GREAT_WHITE_SHARK("Great White Shark", "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!", WATER, SHARK, true),
    //Isle
    @SerializedName("Magma Slug")
    MAGMA_SLUG("Magma Slug", "From beneath the lava appears a Magma Slug.", LAVA, ISLE),
    @SerializedName("Moogma")
    MOOGMA("Moogma", "You hear a faint Moo from the lava... A Moogma appears.", LAVA, ISLE),
    @SerializedName("Lava Leech")
    LAVA_LEECH("Lava Leech", "A small but fearsome Lava Leech emerges.", LAVA, ISLE),
    @SerializedName("Pyroclastic Worm")
    PYROCLASTIC_WORM("Pyroclastic Worm", "You feel the heat radiating as a Pyroclastic Worm surfaces.", LAVA, ISLE),
    @SerializedName("Lava Flame")
    LAVA_FLAME("Lava Flame", "A Lava Flame flies out from beneath the lava.", LAVA, ISLE),
    @SerializedName("Fire Eel")
    FIRE_EEL("Fire Eel", "A Fire Eel slithers out from the depths.", LAVA, ISLE),
    @SerializedName("Taurus")
    TAURUS("Taurus", "Taurus and his steed emerge.", LAVA, ISLE),
    @SerializedName("Plhlegblast")
    PLHLEGBLAST("Plhlegblast", "WOAH! A Plhlegblast appeared.", LAVA, ISLE, true),
    @SerializedName("Thunder")
    THUNDER("Thunder", "You hear a massive rumble as Thunder emerges.", LAVA, ISLE, true),
    @SerializedName("Lord Jawbus")
    JAWBUS("Lord Jawbus", "You have angered a legendary creature... Lord Jawbus has arrived.", LAVA, ISLE, true),
    //Lava Hotspot
    @SerializedName("Fried Chicken")
    FRIED_CHICKEN("Fried Chicken", "Smells of burning. Must be a Fried Chicken.", LAVA, HOTSPOT_LAVA),
    @SerializedName("Fireproof Witch")
    FIREPROOF_WITCH("Fireproof Witch", "Trouble's brewing, it's a Fireproof Witch!", LAVA, HOTSPOT_LAVA),
    @SerializedName("Fiery Scutter")
    FIERY_SCUTTER("Fiery Scutter", "A Fiery Scuttler inconspicuously waddles up to you, friends in tow.", LAVA, HOTSPOT_LAVA, true),
    @SerializedName("Ragnarok")
    RAGNAROK("Ragnarok", "The sky darkens and the air thickens. The end times are upon us: Ragnarok is here.", LAVA, HOTSPOT_LAVA, true);

    fun toDataOption() : DataOption {
        return DataOption(this, this.scName)
    }

    override fun toString(): String {
        return scName
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

        fun isInIslands(sc: SeaCreatures, category: SeaCreatureCategory): Boolean {
            return category.islands.any { it in sc.category.islands }
        }

        fun isInIslands(sc : String, category: SeaCreatureCategory) : Boolean {
            val sc = SeaCreatures.entries.find { it.scName == sc } ?: return false
            return isInIslands(sc, category)
        }
    }
}