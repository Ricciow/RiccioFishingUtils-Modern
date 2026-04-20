package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.config.seacreatures.SeaCreatureSettingsManager
import cloud.glitchdev.rfu.model.data.DataOption
import cloud.glitchdev.rfu.data.fishing.Hotspot
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import net.minecraft.world.phys.Vec3

@JsonAdapter(SeaCreaturesAdapter::class)
class SeaCreatures(
    val scName: String,
    val catchMessage: String,
    val liquidType: LiquidTypes,
    val category: SeaCreatureCategory,
    val condition: (Hotspot?, Vec3, Bait?) -> Boolean = { _, _, _ -> true },
    val lsRangeEnabled: Boolean = true,
    val bossbar: Boolean = false,
    val gdragAlert: Boolean = false,
    val rareSCAlert: Boolean = false,
    val scDisplayColor: String = "§f",
    val weight: Int = 0
) {
    val special: Boolean
        get() = SeaCreatureSettingsManager.isSpecial(scName)
    fun toDataOption(): DataOption = DataOption(this, this.scName)
    fun getSingularNameWithArticle(): String = "${getArticle()} ${getNameWithoutArticle()}"
    fun getArticle(): String = SeaCreatureSettingsManager.getArticle(scName)
    fun getNameWithoutArticle(): String = SeaCreatureSettingsManager.getName(scName)
    fun getPluralName(): String = SeaCreatureSettingsManager.getPlural(scName)
    fun getStyleCode(): String = SeaCreatureSettingsManager.getStyle(scName)

    override fun toString(): String {
        return scName
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SeaCreatures) return false
        return scName == other.scName
    }

    override fun hashCode(): Int {
        return scName.hashCode()
    }

    companion object {
        private val registry = mutableMapOf<String, SeaCreatures>()

        val entries: Collection<SeaCreatures>
            get() = registry.values

        fun get(name: String): SeaCreatures? = registry[name]

        fun register(sc: SeaCreatures) {
            registry[sc.scName] = sc
        }

        fun toDataOptions(
            liquidType: LiquidTypes,
            island: FishingIslands,
            partyType: PartyTypes
        ): ArrayList<DataOption> {
            if (partyType.noMobs) return arrayListOf()

            val seaCreatures = entries.filter { sc ->
                if (sc.liquidType != liquidType) return@filter false
                if (!sc.category.islands.contains(island)) return@filter false
                if (!sc.category.partyTypes.contains(partyType)) return@filter false

                return@filter true
            }
                .sortedWith(
                    compareByDescending<SeaCreatures> { it.special }
                        .thenBy { it.category.islands.size }
                )

            return seaCreatures.map { sc ->
                sc.toDataOption()
            } as ArrayList<DataOption>
        }

        fun isInIslands(sc: SeaCreatures, category: SeaCreatureCategory): Boolean {
            return category.islands.any { it in sc.category.islands }
        }

        fun isInIslands(sc : String, category: SeaCreatureCategory) : Boolean {
            val scObj = get(sc) ?: return false
            return isInIslands(scObj, category)
        }
    }
}

class SeaCreaturesAdapter : TypeAdapter<SeaCreatures>() {
    override fun write(out: JsonWriter, value: SeaCreatures?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.scName)
        }
    }

    override fun read(`in`: JsonReader): SeaCreatures? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val name = `in`.nextString()
        return SeaCreatures.get(name) ?: SeaCreatures(name, "", LiquidTypes.WATER, SeaCreatureCategory.GENERAL_WATER)
    }
}
