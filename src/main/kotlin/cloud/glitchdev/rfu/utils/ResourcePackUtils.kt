package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.config.categories.OtherSettings
import net.minecraft.client.Minecraft
import net.minecraft.network.Connection
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket
import java.io.IOException
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.UUID

object ResourcePackUtils {

    const val BASE_PACK_NAME = "Hypixel Skyblock Server Pack"
    const val PACK_FILENAME_A = "$BASE_PACK_NAME.zip"
    const val PACK_FILENAME_B = "$BASE_PACK_NAME!.zip"

    @JvmStatic
    fun getNextPackFilename(minecraft: Minecraft): String {
        val selected = minecraft.options.resourcePacks
        val isASelected = selected.contains("file/$PACK_FILENAME_A")
        return if (isASelected) PACK_FILENAME_B else PACK_FILENAME_A
    }

    @JvmStatic
    fun isHypixelPackActive(): Boolean {
        val mc = Minecraft.getInstance()
        for (pack in mc.resourcePackRepository.selectedPacks) {
            val id = pack.id
            if (id.startsWith("server/") ||
                id.startsWith("file/Hypixel Server Pack") ||
                id.startsWith("file/$BASE_PACK_NAME")
            ) {
                return true
            }
        }
        return false
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

                    val pathA = resourcePacksDir.resolve(PACK_FILENAME_A)
                    val pathB = resourcePacksDir.resolve(PACK_FILENAME_B)

                    val matchingFilename = when {
                        Files.exists(pathA) && getFileSHA1(pathA) == hash -> PACK_FILENAME_A
                        Files.exists(pathB) && getFileSHA1(pathB) == hash -> PACK_FILENAME_B
                        else -> null
                    }

                    if (matchingFilename != null) {
                        val packNameInOptions = "file/$matchingFilename"
                        val isSelected = minecraft.options.resourcePacks.contains(packNameInOptions)

                        if (!isSelected && OtherSettings.autoLoadResourcePacks) {
                            cleanUpOldVersions(minecraft, matchingFilename)
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
                    val targetFilename = getNextPackFilename(minecraft)
                    val destPack = resourcePacksDir.resolve(targetFilename)

                    Files.copy(pack.path(), destPack, StandardCopyOption.REPLACE_EXISTING)
                    cleanUpOldVersions(minecraft, targetFilename)

                    if (OtherSettings.autoLoadResourcePacks) {
                        val packNameInOptions = "file/$targetFilename"
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
            (name.startsWith("file/Hypixel Server Pack") ||
                    name.startsWith("file/$BASE_PACK_NAME")) && name != currentOptionName
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
                                if ((name.startsWith("Hypixel Server Pack") && name.endsWith(".zip")) ||
                                    (name.startsWith(BASE_PACK_NAME) && name.endsWith(".zip"))
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
                RFULogger.error("Failed to clean up old resource pack versions", e)
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
            val sb = StringBuilder()
            for (b in hashBytes) {
                sb.append(String.format("%02x", b))
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