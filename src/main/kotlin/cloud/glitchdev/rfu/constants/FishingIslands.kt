package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.model.data.DataOption
import com.google.gson.annotations.SerializedName
import java.awt.Color

enum class FishingIslands(val island: String, val availableLiquids : List<LiquidTypes>, val color: Color) {
    @SerializedName("Crimson Isle")
    ISLE("Crimson Isle", listOf(LiquidTypes.LAVA), Color(255, 85, 85)),

    @SerializedName("Crystal Hollows")
    HOLLOWS("Crystal Hollows", listOf(LiquidTypes.LAVA, LiquidTypes.WATER), Color(255, 85, 255)),

    @SerializedName("Backwater Bayou")
    BAYOU("Backwater Bayou", listOf(LiquidTypes.WATER), Color(85, 255, 85)),

    @SerializedName("The Park")
    PARK("The Park", listOf(LiquidTypes.WATER), Color(170, 255, 170)),

    @SerializedName("Galatea")
    GALATEA("Galatea", listOf(LiquidTypes.WATER, LiquidTypes.LAVA), Color(85, 255, 255)),

    @SerializedName("The Farming Islands")
    DESERT("The Farming Islands", listOf(LiquidTypes.WATER), Color(255, 255, 85)),

    @SerializedName("Spider's Den")
    SPIDER("Spider's Den", listOf(LiquidTypes.WATER), Color(170, 170, 170)),

    @SerializedName("Jerry's Workshop")
    JERRY("Jerry's Workshop", listOf(LiquidTypes.WATER), Color(255, 255, 255)),

    @SerializedName("Dwarven Mines")
    DWARVEN("Dwarven Mines", listOf(LiquidTypes.WATER), Color(85, 255, 255)),

    @SerializedName("Hub")
    HUB("Hub", listOf(LiquidTypes.WATER), Color(45, 75, 200)),

    @SerializedName("Other")
    OTHER("Other", listOf(), Color(100, 100, 100)),

    @SerializedName("Not Skyblock")
    NOT_SB("Not Skyblock", listOf(), Color(100, 100, 100));

    fun toDataOption(): DataOption {
        return DataOption(this, this.island)
    }

    companion object {
        fun toDataOptions(): ArrayList<DataOption> {
            return entries.filter { it != OTHER && it != NOT_SB } .map { island ->
                island.toDataOption()
            } as ArrayList<DataOption>
        }

        fun findIslandObject(name : String) : FishingIslands? {
            return entries.find { fishingIslands -> fishingIslands.island == name }
        }
    }
}