package cloud.glitchdev.rfu.model.party

import com.google.gson.annotations.SerializedName
import com.google.gson.Gson

data class FishingParty(
    val user: String,
    val level: Int,
    val title: String,
    val description: String,
    val liquid: String,
    @SerializedName("fishing_type")
    val fishingType: String,
    val requisites : List<Requisite>,
    @SerializedName("sea_creatures")
    val seaCreatures: List<String>,
    val players : Players
) {
    fun getCountString() : String = players.getString()

    fun getSeaCreatureString() : String {
        return seaCreatures.joinToString()
    }

    fun getTitleString() : String {
        return "$title - $user - LVL $level"
    }

    companion object {
        private val gson = Gson()

        fun fromJson(json : String) : FishingParty {
            return gson.fromJson(json, FishingParty::class.java)
        }
    }
}

data class Requisite(
    val id: String,
    val name: String,
    val value: Boolean
)

data class Players(
    val current: Int,
    val max: Int
) {
    fun getString() : String {
        return "$current/$max"
    }
}