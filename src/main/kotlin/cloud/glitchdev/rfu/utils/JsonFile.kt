package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.CONFIG_DIR
import cloud.glitchdev.rfu.RiccioFishingUtils.LOGGER
import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * A generic manager for loading and saving JSON data using Gson.
 *
 * @param T The class type of your config data.
 * @param filename The name of the file (e.g., "my-mod.json").
 * @param type The class object of T (e.g., MyConfig::class.java).
 * @param defaultFactory A function that returns a fresh instance of T with default values.
 */
class JsonFile<T : Any>(
    private val filename: String,
    private val type: Class<T>,
    private val defaultFactory: () -> T
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file: File = CONFIG_DIR.resolve(MOD_ID).resolve("data").resolve(filename).toFile()

    var data: T = defaultFactory()
        private set

    init {
        load()
    }

    fun load() {
        if (file.exists()) {
            try {
                FileReader(file).use { reader ->
                    val loaded = gson.fromJson(reader, type)
                    data = loaded ?: defaultFactory()
                }
            } catch (e: Exception) {
                LOGGER.warn("[$filename] Failed to load json file. Using defaults. Error: ${e.message}")
                data = defaultFactory()
                save()
            }
        } else {
            save()
        }
    }

    fun save() {
        try {
            println("Saved to ${file.absolutePath}")
            file.parentFile.mkdirs()
            FileWriter(file).use { writer ->
                gson.toJson(data, writer)
            }
        } catch (e: Exception) {
            LOGGER.warn("[$filename] Failed to save json file. Error: ${e.message}")
        }
    }
}