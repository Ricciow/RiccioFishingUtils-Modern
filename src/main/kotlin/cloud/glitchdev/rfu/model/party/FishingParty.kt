package cloud.glitchdev.rfu.model.party

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.PartyTypes
import cloud.glitchdev.rfu.constants.SeaCreatures
import com.google.gson.annotations.SerializedName
import com.google.gson.Gson

data class FishingParty(
    var user: String,
    var level: Int,
    var title: String,
    var description: String,
    var liquid: LiquidTypes,
    @SerializedName("fishing_type")
    var fishingType: PartyTypes,
    var island: FishingIslands,
    var requisites : MutableList<Requisite>,
    @SerializedName("sea_creatures")
    var seaCreatures: List<SeaCreatures>,
    var players : Players
) {
    fun getCountString() : String {
        return "${players.getString()} ${island.island} ${liquid.liquid}"
    }

    fun getSeaCreatureString() : String {
        return seaCreatures.joinToString()
    }

    fun getTitleString() : String {
        return "$title - $user - LVL $level"
    }

    fun toJson() : String {
        return gson.toJson(this)
    }

    fun getRequisite(id : String, name: String) : Requisite {
        for (req in requisites) {
            if(req.id == id) return req
        }
        return Requisite(id, name, false)
    }

    fun setRequisite(id : String, name: String, value: Boolean) {
        val requisite = requisites.find { requisite -> requisite.id == id }

        if(requisite != null) {
            requisite.update(name, value)
        }
        else {
            requisites.add(Requisite(id, name, value))
        }
    }

    companion object {
        private val gson = Gson()

        fun fromJson(json : String) : FishingParty {
            return gson.fromJson(json, FishingParty::class.java)
        }

        fun blankParty() : FishingParty {
            return fromJson("{\n" +
                    "  \"user\": \"Usuario top\",\n" +
                    "  \"level\": 200,\n" +
                    "  \"title\": \"Titulo top\",\n" +
                    "  \"description\": \"Decricao top\",\n" +
                    "  \"liquid\": \"Lava\",\n" +
                    "  \"fishing_type\": \"Normal\",\n" +
                    "  \"island\": \"Crimson Isle\",\n" +
                    "  \"requisites\": [\n" +
                    "    {\n" +
                    "      \"id\": \"enderman_9\",\n" +
                    "      \"name\": \"Enderman 9\",\n" +
                    "      \"value\": true\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"brain_food\",\n" +
                    "      \"name\": \"Brain Food\",\n" +
                    "      \"value\": true\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"looting_5\",\n" +
                    "      \"name\": \"Looting 5\",\n" +
                    "      \"value\": true\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"id\": \"has_killer\",\n" +
                    "      \"name\": \"Has killer\",\n" +
                    "      \"value\": true\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"sea_creatures\": [\n" +
                    "    \"Lord Jawbus\", \"Thunder\"\n" +
                    "  ],\n" +
                    "  \"players\": {\n" +
                    "    \"current\": 2,\n" +
                    "    \"max\": 10\n" +
                    "  }\n" +
                    "}")
        }
    }
}

data class Requisite(
    val id: String,
    var name: String,
    var value: Boolean
) {
    fun update(name : String = this.name, value: Boolean = this.value) {
        this.name = name
        this.value = value
    }
}

data class Players(
    val current: Int,
    val max: Int
) {
    fun getString() : String {
        return "$current/$max"
    }
}