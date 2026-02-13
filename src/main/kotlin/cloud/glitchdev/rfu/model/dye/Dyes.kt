package cloud.glitchdev.rfu.model.dye

import cloud.glitchdev.rfu.utils.World
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose

data class Dyes(
    @Expose
    val dye1 : String? = null,
    @Expose
    val dye2 : String? = null,
    @Expose
    val dye3 : String? = null,
    @Expose(serialize = false, deserialize = true)
    val sbYear : Long? = null
) {
    fun isOutdated() : Boolean {
        return isEmpty() || sbYear != World.getCurrentSkyBlockYear()
    }

    fun isEmpty() : Boolean {
        return dye1 == null && dye2 == null && dye3 == null && sbYear == null
    }

    fun toJson(): String {
        return gson.toJson(this)
    }

    fun get3xDye() : String {
        return dye2 ?: "Unknown"
    }

    fun get2xDyes() : List<String> {
        return listOf(dye1 ?: "Unknown", dye3 ?: "Unknown")
    }

    companion object {
        val gson: Gson = GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
    }
}
