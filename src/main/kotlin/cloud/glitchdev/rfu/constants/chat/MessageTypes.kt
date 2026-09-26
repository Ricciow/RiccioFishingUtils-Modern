package cloud.glitchdev.rfu.constants.chat

import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

enum class MessageTypes(val displayName: String, val regex: Regex) {
    HYPE("Hyperion", """Your Implosion hit \d+ enem(?:y|ies) for [\d.,]+ damage\.""".toExactRegex()),
    AUTOPET("Autopet", """(?:Autopet equipped your .+! VIEW RULE|Autopet rule triggered but couldn't find your pet!)""".toExactRegex()),
    LOOTSHARE("Lootshare", """(?:LOOT SHARE You received loot for assisting .+?!|LOOT SHARE You received \d+ .+? Shards? for assisting .+!)""".toExactRegex()),
    CATCH("Catch", """(?:Double Hook!|It's a Double Hook! Woot woot!|It's a Double Hook!)""".toExactRegex()),
    COMBO("Combo", """\+\d+ Kill Combo (.+)""".toExactRegex()),
    BLOCKS("Blocks in the way", """There are blocks in the way!""".toExactRegex()),
    THUNDER_SPARK("Thunder Spark", """Try clicking this Thunder Spark with an Empty Thunder Bottle to collect it!""".toExactRegex()),
    COCOON("Cocoon", """CAUGHT! You cocooned (?:an? )?(.+)!""".toExactRegex()),
    SACKS("Sacks", """\[Sacks\] \+\d+ items?\. \(Last \d+s\.\)""".toExactRegex()),
    CHARM("Charm", """(?:CHARM! You charmed .+ and received .+|LOOT SHARE You received \d+ .+? Shards? for assisting .+!)""".toExactRegex()),
    HURRICANE_BOTTLE("Hurricane in a Bottle", """(?:> )?Your (?:Empty )?Hurricane (?:in a )?Bottle has absorbed .+""".toExactRegex()),
    VANQUISHER("Vanquisher", """A Vanquisher is spawning nearby!""".toExactRegex()),
    TREASURES("Treasures", """(?:\uE025\s*)?(?:GOOD|GREAT|OUTSTANDING) CATCH! You caught .+!""".toExactRegex()),
    BLAZETEKK_RADIO("Blazetekk Radio", """(?:Your radio is weak\. Find another enjoyer to boost it\.|Your radio signal is strong!)\s*""".toExactRegex());

    fun matches(message: String): Boolean {
        if (this == CATCH && SeaCreatures.entries.any { it.catchMessage == message }) return true

        val match = regex.find(message) ?: return false
        return this != COMBO || !match.groupValues[1].contains("Magic Find")
    }

    override fun toString(): String {
        return displayName
    }
}
