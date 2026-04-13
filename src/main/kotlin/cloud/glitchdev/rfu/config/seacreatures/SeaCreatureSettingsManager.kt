package cloud.glitchdev.rfu.config.seacreatures

import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.JsonFile
import com.google.gson.Gson


object SeaCreatureSettingsManager {
    val seaCreatureConfigFile = JsonFile(
        directory = "repo",
        filename = "sc-config.json",
        type = SeaCreatureSettings::class.java,
        defaultFactory = { SeaCreatureSettings.empty() }
    )

    private lateinit var defaultConfig: SeaCreatureSettings
    private val scConfig = seaCreatureConfigFile.data

    fun getName(scName: String): String = resolve(scName) { it.name }
    fun getPlural(scName: String): String = resolve(scName) { it.plural }
    fun getArticle(scName: String): String = resolve(scName) { it.article }
    fun getFormat(scName: String): String = resolve(scName) { it.format }
    fun isSpecial(scName: String): Boolean = resolve(scName) { it.special }

    private fun <T> resolve(
        scName: String,
        extractor: (SeaCreatureSetting) -> T?
    ): T {
        val configured = scConfig.creatures[scName]?.let { extractor(it) }
        if (configured != null) return configured;
        val default = defaultConfig.creatures[scName];
        return extractor(default!!)!!
    }

    fun onInitialize() {
        val gson = Gson()

        val stream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("assets/rfu/defaults/sc-config.json")
            ?: error("Missing resource: assets/rfu/defaults/sc-config.json")

        defaultConfig = gson.fromJson(stream.bufferedReader(), SeaCreatureSettings::class.java)
    }
}