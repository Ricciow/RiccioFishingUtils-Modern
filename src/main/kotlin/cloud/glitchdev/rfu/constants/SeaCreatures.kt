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
    internal val scName: String,
    val scDisplayName: String,
    val article: String,
    val plural: String,
    val style: String,
    val special: Boolean,
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
    fun toDataOption(): DataOption = DataOption(this, this.scDisplayName)
    fun getSingularNameWithArticle(): String = "$article $scDisplayName"

    override fun toString(): String {
        return scDisplayName
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
        private val displayNameRegistry = mutableMapOf<String, SeaCreatures>()

        val entries: Collection<SeaCreatures>
            get() = registry.values

        fun get(name: String): SeaCreatures? = registry[name]

        fun register(sc: SeaCreatures) {
            registry[sc.scName] = sc
            displayNameRegistry[sc.scDisplayName.lowercase()] = sc
        }

        fun isInIslands(sc: SeaCreatures, category: SeaCreatureCategory): Boolean {
            return category.islands.any { it in sc.category.islands }
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
        return SeaCreatures.get(name) ?: SeaCreatures(name, name, "", "", "", false, "", LiquidTypes.WATER, SeaCreatureCategory.GENERAL_WATER)
    }
}
