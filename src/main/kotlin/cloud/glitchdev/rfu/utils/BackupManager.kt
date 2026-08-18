package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.CONFIG_DIR
import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import com.google.gson.JsonObject
import com.teamresourceful.resourcefulconfig.api.patching.ConfigPatchEvent
import com.teamresourceful.resourcefulconfig.common.jsonc.JsoncObject
import com.teamresourceful.resourcefulconfig.common.loader.Patcher
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object BackupManager {
    val backupDir: File = CONFIG_DIR.resolve(MOD_ID).resolve("backups").toFile()
    private val pendingNotifications = ConcurrentLinkedQueue<String>()
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    init {
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        registerJoinEvent(delayMillis = 2000) {
            val notifications = flushNotifications()
            notifications.forEach { msg ->
                Chat.sendMessage(TextUtils.rfuLiteral(msg))
            }
        }
    }

    /**
     * Saves text content as a GZ compressed backup file inside config/rfu/backups/<backupFilePath>.gz.
     */
    fun saveGzBackup(backupFilePath: String, content: String) {
        try {
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            val fileName = if (backupFilePath.endsWith(".gz")) backupFilePath else "$backupFilePath.gz"
            val targetFile = File(backupDir, fileName)
            val parentDir = targetFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }
            val tempFile = File(parentDir ?: backupDir, "${targetFile.name}.tmp")

            FileOutputStream(tempFile).use { fos ->
                GZIPOutputStream(fos).use { gzos ->
                    OutputStreamWriter(gzos, StandardCharsets.UTF_8).use { writer ->
                        writer.write(content)
                    }
                }
            }

            Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            RFULogger.dev("[BackupManager] Saved GZ backup to ${targetFile.name}")
        } catch (e: Exception) {
            RFULogger.warn("[BackupManager] Failed to save GZ backup: ${e.message}", e)
        }
    }

    /**
     * Reads and decompresses a GZ compressed backup file from config/rfu/backups/<backupFilePath>.
     */
    fun readGzBackup(backupFilePath: String): String? {
        val fileName = if (backupFilePath.endsWith(".gz")) backupFilePath else "$backupFilePath.gz"
        val targetFile = File(backupDir, fileName)
        if (!targetFile.exists()) return null

        return try {
            FileInputStream(targetFile).use { fis ->
                GZIPInputStream(fis).use { gzis ->
                    InputStreamReader(gzis, StandardCharsets.UTF_8).use { reader ->
                        reader.readText()
                    }
                }
            }
        } catch (e: Exception) {
            RFULogger.warn("[BackupManager] Failed to read GZ backup ${targetFile.name}: ${e.message}", e)
            null
        }
    }

    /**
     * Writes content to a file atomically using a .tmp file.
     */
    fun writeAtomically(file: File, content: String) {
        writeAtomically(file) { writer -> writer.write(content) }
    }

    /**
     * Writes content to a file atomically using a .tmp file with a custom content writer.
     */
    fun writeAtomically(file: File, contentWriter: (writer: OutputStreamWriter) -> Unit) {
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val tempFile = File(parentDir ?: File("."), "${file.name}.tmp")
        try {
            FileOutputStream(tempFile).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    contentWriter(writer)
                    writer.flush()
                }
            }

            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (e: Exception) {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: Exception) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            throw e
        }
    }

    /**
     * Quarantines a corrupted file by appending .corrupted_<timestamp> into config/rfu/corrupted/.
     */
    fun quarantineCorruptFile(file: File): File? {
        if (!file.exists()) return null
        val corruptedDir = CONFIG_DIR.resolve(MOD_ID).resolve("corrupted").toFile()
        if (!corruptedDir.exists()) {
            corruptedDir.mkdirs()
        }
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val quarantineFile = File(corruptedDir, "${file.name}.corrupted_$timestamp")
        return try {
            Files.move(file.toPath(), quarantineFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            RFULogger.warn("[BackupManager] Quarantined corrupted file ${file.name} to corrupted/${quarantineFile.name}")
            quarantineFile
        } catch (e: Exception) {
            RFULogger.error("[BackupManager] Failed to quarantine corrupted file ${file.name}: ${e.message}", e)
            null
        }
    }

    /**
     * Loads settings config file for RFU. If corrupted, attempts recovery from GZ backup; if backup fails, quarantines it to corrupted/.
     */
    fun loadOrRestoreSettings(
        file: File,
        configId: String,
        version: Int,
        handler: Consumer<ConfigPatchEvent>,
        loadConfigCallback: (JsonObject) -> Unit
    ) {
        if (file.exists()) {
            try {
                val data = file.readText(StandardCharsets.UTF_8)
                var json = JsoncObject.parse(data)
                json = Patcher.patch(json, version, handler)
                loadConfigCallback(json)
                return
            } catch (e: Exception) {
                RFULogger.warn("[$configId] Failed to parse primary settings.jsonc. Attempting backup recovery...", e)
            }
        }

        val backupContent = readGzBackup("settings.jsonc.gz")
        if (backupContent != null) {
            try {
                var json = JsoncObject.parse(backupContent)
                json = Patcher.patch(json, version, handler)
                loadConfigCallback(json)

                writeAtomically(file, backupContent)
                queueNotification("${TextColor.LIGHT_RED}Corrupted ${TextColor.YELLOW}settings.jsonc${TextColor.LIGHT_RED} detected. Restored successfully from ${TextColor.LIGHT_GREEN}backups/settings.jsonc.gz${TextColor.LIGHT_RED}.")
                RFULogger.info("[$configId] Successfully restored settings.jsonc from backups/settings.jsonc.gz")
                return
            } catch (e: Exception) {
                RFULogger.warn("[$configId] Backup settings.jsonc.gz was also corrupted.", e)
            }
        }

        if (file.exists()) {
            quarantineCorruptFile(file)
            queueNotification("${TextColor.LIGHT_RED}Failed to load ${TextColor.YELLOW}settings.jsonc${TextColor.LIGHT_RED} and its backup. Initialized defaults. Corrupted file saved to corrupted folder.")
        }
    }

    /**
     * Queue a chat notification message to be displayed when the player joins the game.
     */
    fun queueNotification(message: String) {
        pendingNotifications.add(message)
    }

    /**
     * Flushes all queued pending chat notifications.
     */
    fun flushNotifications(): List<String> {
        val list = mutableListOf<String>()
        while (true) {
            val msg = pendingNotifications.poll() ?: break
            list.add(msg)
        }
        return list
    }
}
