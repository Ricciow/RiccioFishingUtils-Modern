package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.model.data.DataOption
import com.google.gson.annotations.SerializedName

enum class PartyTypes(val type: String){
    @SerializedName("Normal")
    REGULAR("Normal"),
    @SerializedName("Hotspot")
    HOTSPOT("Hotspot"),
    @SerializedName("Barn")
    BARN("Barn"),
    @SerializedName("Trophy")
    TROPHY("Trophy"),
    @SerializedName("Treasure")
    TREASURE("Treasure");

    companion object {
        fun toDataOptions() : ArrayList<DataOption> {
            return entries.map { party ->
                DataOption(party, party.type)
            } as ArrayList<DataOption>
        }
    }
}