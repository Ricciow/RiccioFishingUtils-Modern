package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.CONFIG_DIR
import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import com.google.gson.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.time.Instant

/**
 * A generic manager for loading and saving JSON data using Gson.
 * Modified to support kotlinx.datetime.Instant serialization.
 */
class JsonFile<T : Any>(
    directory: String = "data",
    private val filename: String,
    private val type: Class<T>,
    private val defaultFactory: () -> T,
    private val onSave: () -> Unit = {},
    private val onReload: () -> Unit = {},
    builder : (GsonBuilder) -> Gson = { it.create() },
    private val revertOnAlpha: Boolean = false,
    ) {
    private val gson: Gson = builder(
        GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant::class.java, JsonSerializer<Instant> { src, _, _ ->
                JsonPrimitive(src.toString())
            })
            .registerTypeAdapter(Instant::class.java, JsonDeserializer { json, _, _ ->
                Instant.parse(json.asString)
            })
    )

    private val file: File = CONFIG_DIR.resolve(MOD_ID).resolve(directory).resolve(filename).toFile()

    var data: T = defaultFactory()
        private set

    init {
        instances.add(this)
        load()

        registerJoinEvent {
            save()
        }

        registerDisconnectEvent {
            save()
        }

        registerShutdownEvent {
            save()
        }

        registerTickEvent(interval = 30 * 60 * 20L) {
            save()
        }
    }

    fun load() {
        if (file.exists()) {
            try {
                FileReader(file).use { reader ->
                    val loaded = gson.fromJson(reader, type)
                    data = loaded ?: defaultFactory()
                }
            } catch (e: Exception) {
                RFULogger.warn("[$filename] Failed to load json file. Using defaults.", e)
                data = defaultFactory()
                save(false)
            }
        } else {
            save(false)
        }
        try {
            onReload()
        } catch (e: Exception) {
            RFULogger.error("[$filename] Error during onReload callback", e)
        }
    }

    fun save(triggerOnSave: Boolean = true) {
        if (revertOnAlpha && isOnAlpha()) {
            RFULogger.dev("[$filename] Skipping save: currently on alpha server.")
            return
        }
        if (triggerOnSave) onSave()
        try {
            RFULogger.dev("Saved to ${file.absolutePath}")
            file.parentFile.mkdirs()
            FileWriter(file).use { writer ->
                gson.toJson(data, writer)
            }
        } catch (e: Exception) {
            RFULogger.warn("[$filename] Failed to save json file.", e)
        }
    }

    companion object {
        private val instances = mutableListOf<JsonFile<*>>()
        private var currentlyOnAlpha = false

        fun reloadAll() {
            instances.filter { it.revertOnAlpha }.forEach {
                try {
                    it.load()
                } catch (e: Exception) {
                    RFULogger.error("Failed to reload ${it.filename}", e)
                }
            }
        }

        fun isOnAlpha(): Boolean {
            if (currentlyOnAlpha) return true
            val ip = mc.currentServer?.ip?.lowercase() ?: return false
            return ip == "alpha.hypixel.net" || ip.endsWith(".alpha.hypixel.net")
        }

        init {
            registerJoinEvent(priority = -100) {
                val ip = mc.currentServer?.ip?.lowercase()
                val isAlpha = ip != null && (ip == "alpha.hypixel.net" || ip.endsWith(".alpha.hypixel.net"))

                if (isAlpha) {
                    currentlyOnAlpha = true
                } else {
                    if (currentlyOnAlpha) {
                        RFULogger.info("[RFU] Leaving Alpha server. Rolling back data...")
                        reloadAll()
                    }
                    currentlyOnAlpha = false
                }
            }

            registerDisconnectEvent(priority = -100) {
                if (currentlyOnAlpha) {
                    RFULogger.info("[RFU] Disconnected from Alpha server. Rolling back data...")
                    reloadAll()
                }
                currentlyOnAlpha = false
            }
        }
    }
}