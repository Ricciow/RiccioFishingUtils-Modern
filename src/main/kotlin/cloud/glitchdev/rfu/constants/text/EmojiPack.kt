package cloud.glitchdev.rfu.constants.text

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipInputStream

internal object EmojiPack {
    private const val DEFAULT_EMOJI_PATH = "/assets/rfu/defaults/emojis"
    private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    private val REQUIRED_SPRITES = setOf("discord", "github", "patreon", "fire")
    private val formattingCode = Regex("&([0-9a-fk-or])", RegexOption.IGNORE_CASE)
    private val logger = LoggerFactory.getLogger("rfu")

    private val startupUpdateStarted = AtomicBoolean(false)

    @Volatile
    private var startupUpdateFuture: CompletableFuture<Bundle?>? = null

    fun updateEmojiPack() {
        if (!startupUpdateStarted.compareAndSet(false, true)) return
        startupUpdateFuture = CompletableFuture.supplyAsync(
            { EmojiUpdater.fetchLatestRelease() },
            { task ->
                Thread(task, "RFU-Emoji-Updater").apply {
                    isDaemon = true
                    start()
                }
            }
        )
    }

    private val bundle: Bundle by lazy { loadActiveBundle() }
    val entries: List<Entry> get() = bundle.entries
    val version: String get() = bundle.version

    private fun loadActiveBundle(): Bundle {
        val updated = awaitStartupUpdate() ?: EmojiUpdater.loadCachedBundle()
        if (updated != null) {
            val resolved = ensureRequiredSprites(updated)
            logger.info("[RFU] Loaded emoji pack {} ({} entries).", resolved.version, resolved.entries.size)
            return resolved
        }

        val bundled = loadBundledBundle()
        logger.info("[RFU] Loaded emoji pack {} ({} entries) from bundled resource.", bundled.version, bundled.entries.size)
        return bundled
    }

    private fun awaitStartupUpdate(): Bundle? {
        val future = startupUpdateFuture ?: return null
        return try {
            future.get(20L, TimeUnit.SECONDS)
        } catch (e: Exception) {
            logger.warn("[RFU] Timed out or failed waiting for startup emoji update: {}", e.message ?: e.javaClass.simpleName)
            null
        }
    }

    private fun ensureRequiredSprites(candidate: Bundle): Bundle {
        val present = candidate.entries.mapNotNullTo(mutableSetOf()) { it.emoji.sprite }
        val missing = REQUIRED_SPRITES - present
        if (missing.isEmpty()) return candidate

        val fallbackEntries = loadBundledBundle().entries.filter { it.emoji.sprite in missing }
        if (fallbackEntries.isEmpty()) return candidate
        logger.warn("[RFU] Emoji pack {} is missing required icons {}, falling back to bundled icons.", candidate.version, missing)
        return candidate.copy(entries = candidate.entries + fallbackEntries)
    }

    private fun loadBundledBundle(): Bundle {
        val entries = REQUIRED_SPRITES.map { sprite ->
            val path = "$DEFAULT_EMOJI_PATH/$sprite.png"
            val stream = checkNotNull(javaClass.getResourceAsStream(path)) { "Missing bundled emoji $path" }
            val texture = stream.use { it.readBytes() }
            validatePng(texture, path)

            val listed = sprite == "fire"
            Entry(
                emoji = EmojiData(
                    sprite = sprite,
                    aliases = listOf(sprite),
                    height = if (listed) 8 else 16,
                    ascent = if (listed) 7 else 11,
                    category = if (listed) "nature" else "icons",
                ),
                texture = texture,
                listed = listed,
            )
        }
        return Bundle(version = "local", entries = entries)
    }

    internal fun parseBundle(bytes: ByteArray): Bundle {
        val files = ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            buildMap {
                while (true) {
                    val file = zip.nextEntry ?: break
                    val name = file.name
                    check(!name.contains("..")) { "Unsafe zip entry path $name" }
                    if (name == "manifest.json" || (name.startsWith("textures/") && !file.isDirectory)) {
                        check(put(name, zip.readBytes()) == null) { "Duplicate emoji bundle file $name" }
                    }
                    zip.closeEntry()
                }
            }
        }
        val manifestJson = checkNotNull(files["manifest.json"]) { "Missing emoji bundle manifest" }
        val manifest = checkNotNull(Gson().fromJson(manifestJson.decodeToString(), Manifest::class.java)) {
            "Empty emoji bundle manifest"
        }
        check(manifest.schemaVersion == 1) { "Unsupported emoji bundle schema ${manifest.schemaVersion}" }
        val emojis = checkNotNull(manifest.emojis).takeIf { it.isNotEmpty() }
            ?: error("Emoji bundle manifest contains no emojis")

        val seenSprites = mutableSetOf<String>()
        val entries = emojis.map { (id, metadata) ->
            check(id.isNotBlank()) { "Emoji ID cannot be blank" }
            check(metadata.height > 0 && metadata.ascent in 0..metadata.height) {
                "Invalid glyph metrics for emoji $id (height=${metadata.height}, ascent=${metadata.ascent})"
            }
            val spriteId = metadata.texture?.let { metadata.spriteId ?: id }
            if (spriteId != null) {
                check(seenSprites.add(spriteId)) { "Duplicate sprite ID $spriteId" }
            }
            val texture = metadata.texture?.let { path ->
                val png = checkNotNull(files[path]) { "Missing texture $path for emoji $id" }
                validatePng(png, path)
                png
            }
            val aliases = metadata.aliases.orEmpty().filter { it.isNotBlank() }
            val customTriggers = metadata.customTriggers.orEmpty().filter { it.isNotBlank() }
            val category = metadata.category?.takeIf { it.isNotBlank() } ?: "general"
            Entry(
                emoji = EmojiData(
                    sprite = spriteId,
                    aliases = listOf(id) + aliases,
                    customTriggers = customTriggers,
                    textReplacement = metadata.textReplacement?.let { text ->
                        formattingCode.replace(text) { "§${it.groupValues[1]}" }
                    },
                    messageReplacement = metadata.messageReplacement,
                    height = metadata.height,
                    ascent = metadata.ascent,
                    category = category,
                ),
                texture = texture,
                listed = metadata.listed,
            )
        }
        val version = manifest.version?.takeIf { it.isNotBlank() } ?: "unknown"
        return Bundle(version = version, entries = entries)
    }

    private fun validatePng(bytes: ByteArray, path: String) {
        check(bytes.size >= 33) { "Truncated PNG texture $path" }
        for (i in PNG_SIGNATURE.indices) {
            check(bytes[i] == PNG_SIGNATURE[i]) { "Invalid PNG signature in $path" }
        }
        val header = ByteBuffer.wrap(bytes, 8, 16)
        val ihdrLength = header.int
        val ihdrType = header.int
        val width = header.int
        val height = header.int
        check(ihdrLength == 13 && ihdrType == 0x49484452) { "Invalid PNG IHDR chunk in $path" }
        check(width > 0 && height > 0) { "Invalid PNG dimensions ${width}x${height} in $path" }
    }

    class Entry(val emoji: EmojiData, val texture: ByteArray?, val listed: Boolean)

    data class Bundle(val version: String, val entries: List<Entry>)

    private data class Manifest(
        val schemaVersion: Int = 0,
        val version: String? = null,
        val emojis: Map<String, Metadata>? = null,
    )

    private data class Metadata(
        val texture: String? = null,
        val spriteId: String? = null,
        val aliases: List<String>? = emptyList(),
        val customTriggers: List<String>? = emptyList(),
        val textReplacement: String? = null,
        val messageReplacement: String? = null,
        val height: Int = 8,
        val ascent: Int = 7,
        val listed: Boolean = true,
        val category: String? = "general",
    )
}
