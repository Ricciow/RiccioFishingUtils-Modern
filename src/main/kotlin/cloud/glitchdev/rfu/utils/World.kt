package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.constants.FishingIslands

object World {
    fun isInSkyblock() : Boolean {
        val tablist = Tablist.getTablistAsStrings()

        val area = tablist.find { it.startsWith("Area: ")}

        return area != null
    }

    /**
     * Gets the current fishing island, defaults to ISLE if the player isn't on a fishing island
     */
    fun getCurrentFishingIsland() : FishingIslands {
        val tablist = Tablist.getTablistAsStrings()

        val area = tablist.find { it.startsWith("Area: ")}
        if(area == null) return FishingIslands.ISLE

        val islandName = area.slice(IntRange(6, area.length-1))

        return FishingIslands.findIslandObject(islandName, FishingIslands.ISLE)
    }
}