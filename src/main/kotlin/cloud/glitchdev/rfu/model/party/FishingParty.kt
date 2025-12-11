package cloud.glitchdev.rfu.model.party

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.PartyTypes
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
    var seaCreatures: List<String>,
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
            return fromJson("{\"user\":\"Usuariotop\",\"level\":200,\"title\":\"Titulotop\",\"description\":\"Decricaotop\",\"liquid\":\"Lava\",\"fishing_type\":\"Normal\",\"island\":\"Crimson Isle\",\"requisites\":[{\"id\":\"enderman_9\",\"name\":\"Eman 9\",\"value\":true},{\"id\":\"brain_food\",\"name\":\"Brain Food\",\"value\":true},{\"id\":\"looting_5\",\"name\":\"Looting 5\",\"value\":true},{\"id\":\"has_killer\",\"name\":\"Has killer\",\"value\":true}],\"sea_creatures\":[\"Jawbus\",\"Thunder\"],\"players\":{\"current\":2,\"max\":10}}")
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