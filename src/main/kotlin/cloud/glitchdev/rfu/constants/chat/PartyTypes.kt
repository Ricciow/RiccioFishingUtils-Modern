package cloud.glitchdev.rfu.constants.chat

import com.google.gson.annotations.SerializedName

enum class PartyTypes(val type: String){
    @SerializedName("Normal")
    REGULAR("Normal"),
    @SerializedName("Hotspot")
    HOTSPOT("Hotspot"),
    @SerializedName("Trophy")
    TROPHY("Trophy"),
    @SerializedName("Treasure")
    TREASURE("Treasure");
}