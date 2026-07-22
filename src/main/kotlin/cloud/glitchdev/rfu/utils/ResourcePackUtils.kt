package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.OtherSettings
import net.minecraft.client.Minecraft
import net.minecraft.network.Connection
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket
import net.minecraft.server.packs.repository.Pack
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.UUID

object ResourcePackUtils {

    const val PACK_FILENAME = "Hypixel Skyblock Server Pack.zip"

    @JvmStatic
    fun isHypixelPackActive(): Boolean {
        val mc = Minecraft.getInstance()
        for (pack in mc.resourcePackRepository.selectedPacks) {
            val id = pack.id
            if (id.startsWith("server/") || 
                id.startsWith("file/Hypixel Server Pack - ") || 
                id.startsWith("file/Hypixel Skyblock Server Pack")
            ) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun getPackFilename(): String {
        return PACK_FILENAME
    }

    @JvmStatic
    fun onHandleResourcePackPush(
        minecraft: Minecraft,
        connection: Connection,
        packId: UUID,
        hash: String,
        urlVal: String
    ): Boolean {
        if (OtherSettings.autoAcceptResourcePacks && World.isOnHypixel) {
            minecraft.execute {
                if (OtherSettings.saveResourcePacks) {
                    val resourcePacksDir = minecraft.gameDirectory.toPath().resolve("resourcepacks")
                    val filename = PACK_FILENAME
                    val destPack = resourcePacksDir.resolve(filename)
                    val packNameInOptions = "file/$filename"
                    val isSelected = minecraft.options.resourcePacks.contains(packNameInOptions)
                    val isLoaded = minecraft.resourcePackRepository.selectedPacks.any { it.id == packNameInOptions }

                    if (Files.exists(destPack) && getFileSHA1(destPack) == hash) {
                        if ((!isSelected || !isLoaded) && OtherSettings.autoLoadResourcePacks) {
                            cleanUpOldVersions(minecraft, filename)
                            minecraft.resourcePackRepository.reload()
                            if (!minecraft.options.resourcePacks.contains(packNameInOptions)) {
                                minecraft.options.resourcePacks.addFirst(packNameInOptions)
                            }
                            minecraft.options.loadSelectedResourcePacks(minecraft.resourcePackRepository)
                            minecraft.options.save()
                            minecraft.reloadResourcePacks()
                        }

                        connection.send(ServerboundResourcePackPacket(packId, ServerboundResourcePackPacket.Action.ACCEPTED))
                        connection.send(ServerboundResourcePackPacket(packId, ServerboundResourcePackPacket.Action.DOWNLOADED))
                        connection.send(ServerboundResourcePackPacket(packId, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED))
                        return@execute
                    }
                }

                val url = parseResourcePackUrl(urlVal)
                if (url == null) {
                    connection.send(ServerboundResourcePackPacket(packId, ServerboundResourcePackPacket.Action.INVALID_URL))
                } else {
                    minecraft.downloadedPackSource.allowServerPacks()
                    minecraft.downloadedPackSource.pushPack(packId, url, hash)
                }
            }
            return true
        }
        return false
    }

    @JvmStatic
    fun onLoadRequestedPacks(
        minecraft: Minecraft,
        packsToLoad: List<net.minecraft.client.resources.server.PackReloadConfig.IdAndPath>
    ): Boolean {
        if (OtherSettings.autoAcceptResourcePacks &&
            OtherSettings.saveResourcePacks &&
            World.isOnHypixel
        ) {
            try {
                val resourcePacksDir = minecraft.gameDirectory.toPath().resolve("resourcepacks")
                if (!Files.exists(resourcePacksDir)) {
                    Files.createDirectories(resourcePacksDir)
                }

                for (pack in packsToLoad) {
                    val filename = PACK_FILENAME
                    val destPack = resourcePacksDir.resolve(filename)

                    cleanUpOldVersions(minecraft, filename)

                    Files.copy(pack.path(), destPack, StandardCopyOption.REPLACE_EXISTING)

                    if (OtherSettings.autoLoadResourcePacks) {
                        val packNameInOptions = "file/$filename"
                        minecraft.resourcePackRepository.reload()
                        if (!minecraft.options.resourcePacks.contains(packNameInOptions)) {
                            minecraft.options.resourcePacks.addFirst(packNameInOptions)
                        }
                        minecraft.options.loadSelectedResourcePacks(minecraft.resourcePackRepository)
                        minecraft.options.save()
                    }
                }
            } catch (e: IOException) {
                RFULogger.error("Failed to copy server resource pack to local resourcepacks folder", e, "[RFU]")
            }
            return true
        }
        return false
    }

    @JvmStatic
    fun cleanUpOldVersions(minecraft: Minecraft, currentFilename: String) {
        val currentOptionName = "file/$currentFilename"
        minecraft.options.resourcePacks.removeIf { name ->
            (name.startsWith("file/Hypixel Server Pack - ") || 
             name.startsWith("file/Hypixel Skyblock Server Pack")) && name != currentOptionName
        }

        if (OtherSettings.deleteOldResourcePacks) {
            try {
                val resourcePacksDir = minecraft.gameDirectory.toPath().resolve("resourcepacks")
                if (Files.exists(resourcePacksDir)) {
                    val toDelete = mutableListOf<Path>()
                    Files.list(resourcePacksDir).use { stream ->
                        stream.forEach { path ->
                            val name = path.fileName.toString()
                            if (name != currentFilename) {
                                if ((name.startsWith("Hypixel Server Pack - ") && name.endsWith(".zip")) ||
                                    name == "Hypixel Server Pack.zip" ||
                                    (name.startsWith("Hypixel Skyblock Server Pack - ") && name.endsWith(".zip"))
                                ) {
                                    toDelete.add(path)
                                }
                            }
                        }
                    }

                    if (toDelete.isNotEmpty()) {
                        Thread {
                            for (attempt in 0 until 10) {
                                try {
                                    Thread.sleep(2000)
                                } catch (e: InterruptedException) {
                                    Thread.currentThread().interrupt()
                                    break
                                }
                                var allDeleted = true
                                for (path in toDelete) {
                                    try {
                                        Files.deleteIfExists(path)
                                    } catch (e: IOException) {
                                        allDeleted = false
                                    }
                                }
                                if (allDeleted) {
                                    break
                                }
                            }
                        }.start()
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    @JvmStatic
    fun getFileSHA1(path: Path): String {
        try {
            val digest = MessageDigest.getInstance("SHA-1")
            Files.newInputStream(path).use { `is` ->
                val buffer = ByteArray(8192)
                var read: Int
                while (`is`.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
            }
            val hashBytes = digest.digest()
            val sb = java.lang.StringBuilder()
            for (b in hashBytes) {
                sb.append(java.lang.String.format("%02x", b))
            }
            return sb.toString()
        } catch (e: Exception) {
            RFULogger.error("Failed to read server resource pack file hash", e, "[RFU]")
            return ""
        }
    }

    @JvmStatic
    fun parseResourcePackUrl(urlString: String): URL? {
        try {
            val url = URI.create(urlString).toURL()
            val protocol = url.protocol
            return if ("http" != protocol && "https" != protocol) null else url
        } catch (e: Exception) {
            return null
        }
    }
}
