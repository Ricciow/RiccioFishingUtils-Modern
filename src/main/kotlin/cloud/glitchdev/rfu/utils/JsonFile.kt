package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.CONFIG_DIR
import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import com.google.gson.*
import java.io.File
import java.io.FileReader
import kotlin.time.Instant

/**
 * A generic manager for loading and saving JSON data using Gson.
 * Modified to support atomic writes and GZ backups.
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
    private val backupFileName: String = if (directory.isBlank()) "$filename.gz" else "$directory/$filename.gz"

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
        var loadedData: T? = null
        val hadFile = file.exists()

        if (hadFile) {
            try {
                FileReader(file).use { reader ->
                    loadedData = gson.fromJson(reader, type)
                }
            } catch (e: Exception) {
                RFULogger.warn("[$filename] Failed to parse primary JSON file. Attempting recovery...", e)
            }
        }

        if (loadedData != null) {
            data = loadedData
        } else {
            val backupContent = BackupManager.readGzBackup(backupFileName)
            var backupData: T? = null

            if (backupContent != null) {
                try {
                    backupData = gson.fromJson(backupContent, type)
                } catch (e: Exception) {
                    RFULogger.warn("[$filename] Backup GZ file $backupFileName was also corrupted.", e)
                }
            }

            if (backupData != null) {
                data = backupData
                try {
                    val jsonString = gson.toJson(data)
                    BackupManager.writeAtomically(file) { writer -> writer.write(jsonString) }
                } catch (e: Exception) {
                    RFULogger.warn("[$filename] Failed to write restored backup back to primary file", e)
                }
                BackupManager.queueNotification("${TextColor.LIGHT_RED}Corrupted ${TextColor.YELLOW}${filename}${TextColor.LIGHT_RED} detected. Restored successfully from ${TextColor.LIGHT_GREEN}backups/$backupFileName${TextColor.LIGHT_RED}.")
                RFULogger.info("[$filename] Successfully restored from backup $backupFileName")
            } else {
                data = defaultFactory()
                if (hadFile) {
                    BackupManager.quarantineCorruptFile(file)
                    BackupManager.queueNotification("${TextColor.LIGHT_RED}Failed to load ${TextColor.YELLOW}${filename}${TextColor.LIGHT_RED} and its backup. Initialized defaults. Corrupted file saved to corrupted folder.")
                } else {
                    save(false)
                }
            }
        }

        try {
            onReload()
        } catch (e: Exception) {
            RFULogger.error("[$filename] Error during onReload callback", e)
        }
    }

    fun save(triggerOnSave: Boolean = true) {
        if (revertOnAlpha && World.isOnAlpha) {
            RFULogger.dev("[$filename] Skipping save: currently on alpha server.")
            return
        }
        if (triggerOnSave) onSave()
        try {
            RFULogger.dev("Saved to ${file.absolutePath}")
            val jsonString = gson.toJson(data)
            BackupManager.saveGzBackup(backupFileName, jsonString)
            BackupManager.writeAtomically(file) { writer ->
                writer.write(jsonString)
            }
        } catch (e: Exception) {
            RFULogger.warn("[$filename] Failed to save json file.", e)
        }
    }

    companion object {
        private val instances = mutableListOf<JsonFile<*>>()

        fun reloadAll() {
            instances.filter { it.revertOnAlpha }.forEach {
                try {
                    it.load()
                } catch (e: Exception) {
                    RFULogger.error("Failed to reload ${it.filename}", e)
                }
            }
        }
    }
}