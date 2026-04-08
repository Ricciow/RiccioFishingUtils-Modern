package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.Sounds
import net.minecraft.SharedConstants
import net.minecraft.server.packs.PackType
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.nameWithoutExtension
import kotlin.use
import cloud.glitchdev.rfu.events.managers.PackSelectionScreenEvents.registerPackSelectionScreenTickEvent
import java.nio.file.attribute.FileTime

@RFUFeature
object ResourcePackFeature : Feature {
    private var lastUpdate: FileTime? = null
    private val soundsDir by lazy { mc.gameDirectory.toPath().resolve("resourcepacks/RFU Custom Sounds/assets/$MOD_ID/sounds") }

    override fun onInitialize() {
        generate(Sounds.registeredSounds)
        try {
            if (Files.exists(soundsDir)) {
                lastUpdate = Files.getLastModifiedTime(soundsDir)
            }
        } catch (e: Exception) {
            RFULogger.error("Error while loading sounds", e)
        }

        registerPackSelectionScreenTickEvent {
            if (Files.exists(soundsDir)) {
                val lastModified = Files.getLastModifiedTime(soundsDir)
                if (lastUpdate == null || lastModified > lastUpdate) {
                    generate(Sounds.registeredSounds)
                    lastUpdate = lastModified
                }
            } else if (lastUpdate != null) {
                generate(Sounds.registeredSounds)
                lastUpdate = null
            }
        }
    }



    fun generate(soundNames: Set<String>) {
        try {
            val rpDir = soundsDir.parent.parent.parent

            if (!Files.exists(rpDir)) {
                Files.createDirectories(rpDir)
            }

            val packFormat = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).major
            val packMcmeta = rpDir.resolve("pack.mcmeta")
            val packMcmetaContent = """
                {
                  "pack": {
                    "pack_format": $packFormat,
                    "min_format": $packFormat,
                    "max_format": 999,
                    "description": "Custom sounds for RFU"
                  }
                }
            """.trimIndent()
            Files.writeString(packMcmeta, packMcmetaContent)

            val soundsDir = rpDir.resolve("assets/$MOD_ID/sounds")
            Files.createDirectories(soundsDir)

            val packPng = rpDir.resolve("pack.png")
            if (!Files.exists(packPng)) {
                val iconStream = ResourcePackFeature::class.java.getResourceAsStream("/assets/$MOD_ID/icon.png")
                if (iconStream != null) {
                    Files.copy(iconStream, packPng, StandardCopyOption.REPLACE_EXISTING)
                    iconStream.close()
                }
            }

            val soundsJsonFile = rpDir.resolve("assets/$MOD_ID/sounds.json")
            val customSounds = Files.list(soundsDir).use { stream ->
                stream.filter { it.toString().lowercase().endsWith(".ogg") }
                    .map { it.nameWithoutExtension.lowercase() }
                    .filter { soundNames.contains(it) }
                    .toList()
            }

            if (customSounds.isNotEmpty()) {
                val soundsEntries = customSounds.joinToString(",\n") { name ->
                    """
                    |  "$name": {
                    |    "sounds": ["$MOD_ID:$name"]
                    |  }
                    """.trimMargin()
                }
                Files.writeString(soundsJsonFile, "{\n$soundsEntries\n}")
            } else if (Files.exists(soundsJsonFile)) {
                Files.delete(soundsJsonFile)
            }

            val noteFile = rpDir.resolve("note.txt")
            val currentSoundsList = soundNames.sorted().joinToString("\n") { "- $it" }

            val noteContent = "Welcome to the RFU Custom Sounds resource pack!\n" +
                    "\n" +
                    "To use your own sounds:\n" +
                    "1. Place your .ogg files in: assets/$MOD_ID/sounds/\n" +
                    "2. Name them EXACTLY like one of the sounds in the list below.\n" +
                    "3. Enable this resource pack in Minecraft settings.\n" +
                    "\n" +
                    "Available sound names (Replace any of these with your own .ogg file):\n" +
                    "$currentSoundsList\n" +
                    "\n" +
                    "IMPORTANT NOTES:\n" +
                    "- Minecraft is very picky about .ogg files. Ensure they are exported correctly (Mono, 44100Hz is safest) or they may cause crashes.\n" +
                    "- This file is automatically updated by the mod when new sounds are added.\n" +
                    "- The sounds.json file is automatically generated by the mod and will be overwritten."

            if (!Files.exists(noteFile)) {
                Files.writeString(noteFile, noteContent)
            } else {
                val existingContent = Files.readString(noteFile)
                val missing = soundNames.filter { !existingContent.contains(it) }
                if (missing.isNotEmpty()) {
                    Files.writeString(noteFile, noteContent)
                }
            }
        } catch (e: Exception) {
            RFULogger.error("Failed to generate custom sounds resource pack", e)
        }
    }
}
