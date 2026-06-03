package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.model.data.DataOption
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.awt.Color

@JsonAdapter(FishingIslands.Adapter::class)
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
    GALATEA("Galatea", listOf(LiquidTypes.WATER, LiquidTypes.LAVA), Color(10, 100, 0)),

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

    @SerializedName("Lotus Atoll")
    ATOLL("Lotus Atoll", listOf(LiquidTypes.WATER), Color(180, 100, 0)),

    @SerializedName("Other")
    OTHER("Other", listOf(), Color(100, 100, 100)),

    @SerializedName("Not Skyblock")
    NOT_SB("Not Skyblock", listOf(), Color(100, 100, 100)),

    @SerializedName("Unknown")
    UNKNOWN("Unknown", listOf(LiquidTypes.WATER, LiquidTypes.LAVA), Color(150, 150, 150));

    fun toDataOption(): DataOption {
        return DataOption(this, this.island)
    }

    companion object {
        fun toDataOptions(): ArrayList<DataOption> {
            return entries.filter { it != OTHER && it != NOT_SB && it != UNKNOWN } .map { island ->
                island.toDataOption()
            } as ArrayList<DataOption>
        }

        fun findIslandObject(name : String) : FishingIslands? {
            return entries.find { fishingIslands -> fishingIslands.island == name }
        }
    }

    class Adapter : TypeAdapter<FishingIslands>() {
        override fun write(out: JsonWriter, value: FishingIslands?) {
            out.value(value?.island ?: "Unknown")
        }

        override fun read(`in`: JsonReader): FishingIslands {
            val name = `in`.nextString()
            return entries.find { it.island == name } ?: UNKNOWN
        }
    }
}