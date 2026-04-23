package cloud.glitchdev.rfu.constants

import java.awt.Color

enum class HotspotType(val displayName: String, val buffMatch: String?, val color: Color) {
    DOUBLE_HOOK("Double Hook Chance", "Double Hook Chance", Color(85, 85, 255, 100)),
    SEA_CREATURE("Sea Creature Chance", "Sea Creature Chance", Color(0, 170, 170, 100)),
    FISHING_SPEED("Fishing Speed", "Fishing Speed", Color(85, 255, 255, 100)),
    UNKNOWN("Unknown", null, Color(255, 255, 255, 100)),
    TROPHY_FISH("Trophy Fish Chance", "Trophy Fish Chance", Color(255, 170, 0, 100)),
    TREASURE("Treasure Chance", "Treasure Chance", Color(255, 255, 85, 100));

    override fun toString(): String = displayName

    companion object {
        fun fromBuff(buff: String): HotspotType {
            return entries.find { it.buffMatch != null && buff.contains(it.buffMatch) } ?: UNKNOWN
        }
    }
}
