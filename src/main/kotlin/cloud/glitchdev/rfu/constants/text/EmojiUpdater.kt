package cloud.glitchdev.rfu.constants.text

import cloud.glitchdev.rfu.config.categories.OtherSettings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat

internal object EmojiUpdater {
    const val DEFAULT_LATEST_RELEASE_URL = "https://api.github.com/repos/Ricciow/RFU-Emojis/releases/latest"
    private const val BUNDLE_FILE = "emojis.zip"
    private const val CACHE_META_NAME = "emojis-meta.json"
    private val SHA256_REGEX = Regex("^[0-9a-fA-F]{64}$")

    private val logger = LoggerFactory.getLogger("rfu")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val defaultHttpClient: HttpClient by lazy {
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }

    internal fun defaultCacheDir(): Path = FabricLoader.getInstance().configDir.resolve("rfu/repo")

    internal fun configuredLatestReleaseUrl(): String {
        if (!OtherSettings.useCustomEmojiRepo) return DEFAULT_LATEST_RELEASE_URL
        val url = OtherSettings.emojiReleaseUrl.trim()
        val scheme = runCatching { URI.create(url).scheme?.lowercase() }.getOrNull()
        if (scheme != "https" && scheme != "http") {
            logger.warn("[RFU] Invalid custom emoji release URL '{}'; using default.", url)
            return DEFAULT_LATEST_RELEASE_URL
        }
        return url
    }

    private fun userAgent(): String {
        val version = runCatching {
            FabricLoader.getInstance().getModContainer("rfu")
                .map { it.metadata.version.friendlyString }
                .orElse("unknown")
        }.getOrDefault("unknown")
        return "RiccioFishingUtils/$version"
    }

    internal fun loadCachedBundle(cacheDir: Path = defaultCacheDir()): EmojiPack.Bundle? {
        val meta = readMetadata(cacheDir.resolve(CACHE_META_NAME))
        return readValidCache(cacheDir.resolve(BUNDLE_FILE), meta)?.let(::parseQuietly)
    }

    internal fun fetchLatestRelease(
        cacheDir: Path = defaultCacheDir(),
        latestReleaseUrl: String = configuredLatestReleaseUrl(),
        client: HttpClient = defaultHttpClient,
    ): EmojiPack.Bundle? {
        val zipPath = cacheDir.resolve(BUNDLE_FILE)
        val metaPath = cacheDir.resolve(CACHE_META_NAME)
        val cachedMeta = readMetadata(metaPath)
        val cachedBytes = readValidCache(zipPath, cachedMeta)
        // Only trust cached tag/etag when the cache came from the same source repo.
        val comparableMeta = cachedMeta?.takeIf { (it.source ?: DEFAULT_LATEST_RELEASE_URL) == latestReleaseUrl }

        return try {
            fetchLatestReleaseInternal(zipPath, metaPath, comparableMeta, cachedBytes, latestReleaseUrl, client)
        } catch (e: Exception) {
            logger.warn("[RFU] Failed to check for remote emoji updates: {}", e.message ?: e.javaClass.simpleName)
            cachedBytes?.let(::parseQuietly)
        }
    }

    private fun fetchLatestReleaseInternal(
        zipPath: Path,
        metaPath: Path,
        cachedMeta: CacheMetadata?,
        cachedBytes: ByteArray?,
        latestReleaseUrl: String,
        client: HttpClient,
    ): EmojiPack.Bundle? {
        val cachedBundle = { cachedBytes?.let(::parseQuietly) }
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(latestReleaseUrl))
            .timeout(Duration.ofSeconds(5))
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", userAgent())
            .GET()

        if (cachedBytes != null && !cachedMeta?.etag.isNullOrBlank()) {
            requestBuilder.header("If-None-Match", cachedMeta.etag)
        }

        val response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        when (response.statusCode()) {
            304 -> {
                logger.info("[RFU] Emoji pack is up to date ({}).", cachedMeta?.tag ?: "cached")
                return cachedBundle()
            }
            200 -> Unit
            else -> {
                logger.warn("[RFU] Emoji release check returned HTTP {}.", response.statusCode())
                return cachedBundle()
            }
        }

        val release = gson.fromJson(response.body(), GitHubRelease::class.java) ?: return cachedBundle()
        val tagName = release.tagName?.takeIf { it.isNotBlank() } ?: return cachedBundle()
        val bundleAsset = release.assets.orEmpty().firstOrNull { it.name == BUNDLE_FILE }
        val downloadUrl = bundleAsset?.browserDownloadUrl?.takeIf { it.isNotBlank() }
        if (bundleAsset == null || downloadUrl == null) {
            logger.warn("[RFU] Emoji release {} is missing {} asset.", tagName, BUNDLE_FILE)
            return cachedBundle()
        }

        val responseEtag = response.headers().firstValue("etag").orElse(null)?.takeIf { it.isNotBlank() }
        val expectedSha256 = parseDigestSha256(bundleAsset.digest)

        if (cachedBytes != null && cachedMeta != null && isAlreadyUpToDate(cachedMeta, tagName, expectedSha256)) {
            if (responseEtag != cachedMeta.etag || cachedMeta.tag != tagName) {
                writeMetadata(metaPath, cachedMeta.copy(tag = tagName, etag = responseEtag ?: cachedMeta.etag, source = latestReleaseUrl))
            }
            logger.info("[RFU] Emoji pack is up to date ({}).", tagName)
            return cachedBundle()
        }

        val allowHttp = URI.create(latestReleaseUrl).scheme.equals("http", ignoreCase = true)
        val zipBytes = downloadBytes(client, downloadUrl, Duration.ofSeconds(15), allowHttp)
        val actualSha256 = sha256Hex(zipBytes)

        if (expectedSha256 != null && !actualSha256.equals(expectedSha256, ignoreCase = true)) {
            logger.warn("[RFU] Downloaded emoji pack checksum mismatch for {}.", tagName)
            return cachedBundle()
        }

        val bundle = EmojiPack.parseBundle(zipBytes)
        writeBytesAtomically(zipPath, zipBytes)
        writeMetadata(metaPath, CacheMetadata(tag = tagName, etag = responseEtag, sha256 = actualSha256, source = latestReleaseUrl))
        logger.info("[RFU] Updated emoji pack to {} ({} entries).", tagName, bundle.entries.size)
        return bundle
    }

    private fun readValidCache(zipPath: Path, meta: CacheMetadata?): ByteArray? {
        if (!Files.isRegularFile(zipPath)) return null
        return try {
            val bytes = Files.readAllBytes(zipPath)
            val expectedSha = meta?.sha256?.takeIf { SHA256_REGEX.matches(it) }
            if (expectedSha != null && !sha256Hex(bytes).equals(expectedSha, ignoreCase = true)) {
                logger.warn("[RFU] Cached emoji pack checksum mismatch; ignoring cached bundle.")
                return null
            }
            bytes
        } catch (e: Exception) {
            logger.warn("[RFU] Failed to read cached emoji pack: {}", e.message ?: e.javaClass.simpleName)
            null
        }
    }

    private fun parseQuietly(bytes: ByteArray): EmojiPack.Bundle? {
        return try {
            EmojiPack.parseBundle(bytes)
        } catch (e: Exception) {
            logger.warn("[RFU] Failed to parse cached emoji pack: {}", e.message ?: e.javaClass.simpleName)
            null
        }
    }

    private fun isAlreadyUpToDate(cachedMeta: CacheMetadata, latestTag: String, expectedSha256: String?): Boolean {
        if (expectedSha256 != null && cachedMeta.sha256.equals(expectedSha256, ignoreCase = true)) {
            return true
        }
        return cachedMeta.tag == latestTag && expectedSha256 == null
    }

    private fun downloadBytes(
        client: HttpClient,
        url: String,
        timeout: Duration,
        allowHttp: Boolean,
    ): ByteArray {
        val uri = URI.create(url)
        val scheme = uri.scheme?.lowercase()
        check(scheme == "https" || (allowHttp && scheme == "http")) { "Unsupported download URL scheme: $scheme" }

        val request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(timeout)
            .header("User-Agent", userAgent())
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        check(response.statusCode() == 200) { "Download returned HTTP ${response.statusCode()}" }
        return response.body()
    }

    private fun parseDigestSha256(digest: String?): String? {
        if (digest == null || !digest.startsWith("sha256:", ignoreCase = true)) return null
        val hex = digest.substring("sha256:".length).trim()
        return hex.takeIf { SHA256_REGEX.matches(it) }?.lowercase()
    }

    private fun readMetadata(path: Path): CacheMetadata? {
        if (!Files.isRegularFile(path)) return null
        return try {
            gson.fromJson(Files.readString(path, StandardCharsets.UTF_8), CacheMetadata::class.java)
        } catch (_: Exception) {
            null
        }
    }

    private fun writeMetadata(path: Path, metadata: CacheMetadata) {
        writeBytesAtomically(path, gson.toJson(metadata).toByteArray(StandardCharsets.UTF_8))
    }

    private fun writeBytesAtomically(target: Path, bytes: ByteArray) {
        val parent = target.parent
        if (parent != null) Files.createDirectories(parent)
        val temp = (parent ?: Path.of(".")).resolve("${target.fileName}.tmp")
        try {
            Files.write(temp, bytes)
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: Exception) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: Exception) {
            Files.deleteIfExists(temp)
            throw e
        }
    }

    internal fun sha256Hex(bytes: ByteArray): String =
        HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))

    private data class CacheMetadata(
        val tag: String? = null,
        val etag: String? = null,
        val sha256: String? = null,
        val source: String? = null,
    )

    private data class GitHubRelease(
        @SerializedName("tag_name")
        val tagName: String? = null,
        val assets: List<GitHubAsset>? = null,
    )

    private data class GitHubAsset(
        val name: String? = null,
        val digest: String? = null,
        @SerializedName("browser_download_url")
        val browserDownloadUrl: String? = null,
    )
}
