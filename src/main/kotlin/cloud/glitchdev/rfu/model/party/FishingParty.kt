package cloud.glitchdev.rfu.model.party

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.constants.PartyTypes
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.World
import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import kotlin.math.max

data class FishingParty(
    @Transient
    var id: String = "?",
    var user: String,
    var level: Int,
    var title: String,
    var description: String,
    var liquid: LiquidTypes,
    @SerializedName("fishing_type")
    var fishingType: PartyTypes,
    var island: FishingIslands,
    var requisites: MutableList<Requisite>,
    @SerializedName("sea_creatures")
    var seaCreatures: List<SeaCreatures>,
    var players: Players
) {
    fun getCountString(): String {
        return "${players.getString()} ${island.island} ${liquid.liquid}"
    }

    fun getSeaCreatureString(): String {
        return seaCreatures.joinToString { it.scName }
    }

    fun getTitleString(): String {
        return listOf(title, user)
            .filter { it.isNotEmpty() }
            .joinToString(" - ") + " - LVL $level"
    }

    fun toJson(): String {
        return gson.toJson(this)
    }

    fun getRequisite(id: String, name: String): Requisite {
        for (req in requisites) {
            if (req.id == id) return req
        }
        return Requisite(id, name, false)
    }

    fun setRequisite(id: String, name: String, value: Boolean) {
        val requisite = requisites.find { requisite -> requisite.id == id }

        if (requisite != null) {
            requisite.update(name, value)
        } else {
            requisites.add(Requisite(id, name, value))
        }
    }

    companion object {
        private val gson = Gson()

        fun fromJson(json: String): FishingParty {
            return gson.fromJson(json, FishingParty::class.java)
        }

        fun blankParty(): FishingParty {
            val island = World.getCurrentFishingIsland()
            return FishingParty(
                "?",
                User.getUsername(),
                0,
                "",
                "",
                island.availableLiquids.getOrNull(0) ?: LiquidTypes.LAVA,
                PartyTypes.REGULAR,
                island,
                mutableListOf(
                    Requisite("has_killer", "Has Killer", false),
                    Requisite("enderman_9", "Enderman 9", false),
                    Requisite("looting_5", "Looting 5", false),
                    Requisite("brain_food", "Brain Food", false),
                ),
                listOf(),
                Players(max(Party.members.size, 1), 6)
            )
        }
    }
}

data class Requisite(
    val id: String,
    var name: String,
    var value: Boolean
) {
    fun update(name: String = this.name, value: Boolean = this.value) {
        this.name = name
        this.value = value
    }
}

data class Players(
    var current: Int,
    var max: Int
) {
    fun getString(): String {
        return "$current/$max"
    }
}