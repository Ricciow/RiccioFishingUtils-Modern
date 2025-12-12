package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.model.data.DataOption
import com.google.gson.annotations.SerializedName

enum class FishingIslands(val island: String, val availableLiquids : List<LiquidTypes>) {
    @SerializedName("Crimson Isle")
    ISLE("Crimson Isle", listOf(LiquidTypes.LAVA)),

    @SerializedName("Crystal Hollows")
    HOLLOWS("Crystal Hollows", listOf(LiquidTypes.LAVA, LiquidTypes.WATER)),

    @SerializedName("Backwater Bayou")
    BAYOU("Backwater Bayou", listOf(LiquidTypes.WATER)),

    @SerializedName("Park")
    PARK("Park", listOf(LiquidTypes.WATER)),

    @SerializedName("Galatea")
    GALATEA("Galatea", listOf(LiquidTypes.WATER, LiquidTypes.LAVA)),

    @SerializedName("The Farming Islands")
    DESERT("The Farming Islands", listOf(LiquidTypes.WATER)),

    @SerializedName("Spider's Den")
    SPIDER("Spider's Den", listOf(LiquidTypes.WATER)),

    @SerializedName("Jerry's Workshop")
    JERRY("Jerry's Workshop", listOf(LiquidTypes.WATER)),

    @SerializedName("Dwarven Mines")
    DWARVEN("Dwarven Mines", listOf(LiquidTypes.WATER)),

    @SerializedName("Hub")
    HUB("Hub", listOf(LiquidTypes.WATER)),

    @SerializedName("Other")
    OTHER("Other", listOf()),

    @SerializedName("Not Skyblock")
    NOT_SB("Not Skyblock", listOf());

    fun toDataOption(): DataOption {
        return DataOption(this, this.island)
    }

    companion object {
        fun toDataOptions(): ArrayList<DataOption> {
            return entries.filter { it != OTHER && it != NOT_SB } .map { island ->
                island.toDataOption()
            } as ArrayList<DataOption>
        }

        fun findIslandObject(name : String, default : FishingIslands = OTHER) : FishingIslands {
            return entries.find { fishingIslands -> fishingIslands.island == name } ?: default
        }
    }
}