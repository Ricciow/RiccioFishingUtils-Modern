package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.model.data.DataOption
import com.google.gson.annotations.SerializedName

enum class FishingIslands(val island: String) {
    @SerializedName("Crimson Isle")
    ISLE("Crimson Isle"),

    @SerializedName("Crystal Hollows")
    HOLLOWS("Crystal Hollows"),

    @SerializedName("Backwater Bayou")
    BAYOU("Backwater Bayou"),

    @SerializedName("Park")
    PARK("Park"),

    @SerializedName("Galatea")
    GALATEA("Galatea"),

    @SerializedName("The Farming Islands")
    DESERT("The Farming Islands"),

    @SerializedName("Spider's Den")
    SPIDER("Spider's Den"),

    @SerializedName("Jerry's Workshop")
    JERRY("Jerry's Workshop"),

    @SerializedName("Dwarven Mines")
    DWARVEN("Dwarven Mines"),

    @SerializedName("Hub")
    HUB("Hub");

    fun toDataOption(): DataOption {
        return DataOption(this, this.island)
    }

    companion object {
        fun toDataOptions(): ArrayList<DataOption> {
            return entries.map { island ->
                island.toDataOption()
            } as ArrayList<DataOption>
        }
    }
}