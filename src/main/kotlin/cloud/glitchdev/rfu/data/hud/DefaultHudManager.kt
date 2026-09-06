package cloud.glitchdev.rfu.data.hud

import cloud.glitchdev.rfu.RiccioFishingUtils.CONFIG_DIR
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object DefaultHudManager {
    private const val DEFAULTS_RESOURCE_PATH = "assets/rfu/defaults/hud.json"
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    var defaultLayout: DefaultHudLayout = loadDefaults()
        private set

    private fun loadDefaults(): DefaultHudLayout {
        try {
            val exportFile = CONFIG_DIR.resolve("rfu/default_hud_export.json").toFile()
            if (exportFile.exists()) {
                val layout = gson.fromJson(exportFile.readText(), DefaultHudLayout::class.java)
                if (layout != null && layout.elements.isNotEmpty()) {
                    return layout
                }
            }
        } catch (e: Exception) {
            RFULogger.error("Failed to load exported default HUD layout from config", e)
        }

        val classLoader = Thread.currentThread().contextClassLoader ?: DefaultHudManager::class.java.classLoader
        val stream = classLoader?.getResourceAsStream(DEFAULTS_RESOURCE_PATH)
            ?: return DefaultHudLayout()

        return try {
            stream.bufferedReader().use { reader ->
                gson.fromJson(reader, DefaultHudLayout::class.java) ?: DefaultHudLayout()
            }
        } catch (e: Exception) {
            RFULogger.error("Failed to load default HUD layout from resources", e)
            DefaultHudLayout()
        }
    }

    fun calculateDefaultPosition(
        element: AbstractHudElement,
        screenWidth: Float,
        screenHeight: Float
    ): CalculatedHudPosition {
        val id = element.id
        val entry = defaultLayout.elements[id]

        if (entry == null) {
            return calculateFallbackPosition(element, screenWidth, screenHeight)
        }

        val refW = if (defaultLayout.referenceWidth > 0f) defaultLayout.referenceWidth else 960f
        val refH = if (defaultLayout.referenceHeight > 0f) defaultLayout.referenceHeight else 540f

        val scaleRatio = min(screenWidth / refW, screenHeight / refH).coerceIn(0.6f, 1.2f)
        val finalScale = round((entry.scale * scaleRatio) * 1000f) / 1000f

        val scaleFactor = if (entry.scale > 0f) finalScale / entry.scale else 1f
        val actualW = if (entry.width > 0f) entry.width * scaleFactor else element.getWidth()
        val actualH = if (entry.height > 0f) entry.height * scaleFactor else element.getHeight()

        val screenX = when (entry.anchor) {
            HudAnchor.TOP_LEFT, HudAnchor.MIDDLE_LEFT, HudAnchor.BOTTOM_LEFT -> entry.x
            HudAnchor.TOP_CENTER, HudAnchor.CENTER, HudAnchor.BOTTOM_CENTER -> (screenWidth - actualW) / 2f + entry.x
            HudAnchor.TOP_RIGHT, HudAnchor.MIDDLE_RIGHT, HudAnchor.BOTTOM_RIGHT -> screenWidth - actualW - entry.x
        }

        val screenY = when (entry.anchor) {
            HudAnchor.TOP_LEFT, HudAnchor.TOP_CENTER, HudAnchor.TOP_RIGHT -> entry.y
            HudAnchor.MIDDLE_LEFT, HudAnchor.CENTER, HudAnchor.MIDDLE_RIGHT -> (screenHeight - actualH) / 2f + entry.y
            HudAnchor.BOTTOM_LEFT, HudAnchor.BOTTOM_CENTER, HudAnchor.BOTTOM_RIGHT -> screenHeight - actualH - entry.y
        }

        val clampedX = screenX.coerceIn(0f, max(0f, screenWidth - actualW))
        val clampedY = screenY.coerceIn(0f, max(0f, screenHeight - actualH))

        return CalculatedHudPosition(clampedX, clampedY, finalScale)
    }

    private fun calculateFallbackPosition(
        element: AbstractHudElement,
        screenWidth: Float,
        screenHeight: Float
    ): CalculatedHudPosition {
        val x = element.defaultX.coerceIn(0f, max(0f, screenWidth - 100f))
        val y = element.defaultY.coerceIn(0f, max(0f, screenHeight - 30f))
        return CalculatedHudPosition(x, y, 1f)
    }

    fun exportLayoutToJson(
        screenWidth: Float,
        screenHeight: Float,
        elements: List<AbstractHudElement>
    ): String {
        val elementEntries = LinkedHashMap<String, DefaultHudElementEntry>()

        for (element in elements) {
            val w = element.getWidth()
            val h = element.getHeight()
            val curX = element.currentX
            val curY = element.currentY
            val centerX = curX + (w / 2f)
            val centerY = curY + (h / 2f)

            val anchor = detectAnchor(centerX, centerY, screenWidth, screenHeight)

            val offsetX = when (anchor) {
                HudAnchor.TOP_LEFT, HudAnchor.MIDDLE_LEFT, HudAnchor.BOTTOM_LEFT -> curX
                HudAnchor.TOP_CENTER, HudAnchor.CENTER, HudAnchor.BOTTOM_CENTER -> curX - ((screenWidth - w) / 2f)
                HudAnchor.TOP_RIGHT, HudAnchor.MIDDLE_RIGHT, HudAnchor.BOTTOM_RIGHT -> screenWidth - w - curX
            }

            val offsetY = when (anchor) {
                HudAnchor.TOP_LEFT, HudAnchor.TOP_CENTER, HudAnchor.TOP_RIGHT -> curY
                HudAnchor.MIDDLE_LEFT, HudAnchor.CENTER, HudAnchor.MIDDLE_RIGHT -> curY - ((screenHeight - h) / 2f)
                HudAnchor.BOTTOM_LEFT, HudAnchor.BOTTOM_CENTER, HudAnchor.BOTTOM_RIGHT -> screenHeight - h - curY
            }

            val roundedX = round(offsetX * 10f) / 10f
            val roundedY = round(offsetY * 10f) / 10f
            val roundedScale = round(element.scale * 1000f) / 1000f
            val roundedW = round(w * 10f) / 10f
            val roundedH = round(h * 10f) / 10f

            elementEntries[element.id] = DefaultHudElementEntry(
                anchor = anchor,
                x = roundedX,
                y = roundedY,
                scale = roundedScale,
                width = roundedW,
                height = roundedH
            )
        }

        val layout = DefaultHudLayout(
            referenceWidth = screenWidth,
            referenceHeight = screenHeight,
            elements = elementEntries
        )

        defaultLayout = layout

        val jsonString = gson.toJson(layout)

        try {
            mc.keyboardHandler.clipboard = jsonString
        } catch (e: Exception) {
            RFULogger.error("Failed to copy HUD export to clipboard", e)
        }

        try {
            val exportDir = CONFIG_DIR.resolve("rfu").toFile()
            if (!exportDir.exists()) exportDir.mkdirs()
            val exportFile = File(exportDir, "default_hud_export.json")
            exportFile.writeText(jsonString)
        } catch (e: Exception) {
            RFULogger.error("Failed to write HUD export to file", e)
        }

        Chat.sendMessage(
            TextUtils.rfuLiteral("Exported layout with §e${elementEntries.size}§r elements to clipboard and §7config/rfu/default_hud_export.json§r.")
        )

        return jsonString
    }

    private fun detectAnchor(centerX: Float, centerY: Float, screenWidth: Float, screenHeight: Float): HudAnchor {
        val isLeft = centerX < screenWidth * 0.35f
        val isRight = centerX > screenWidth * 0.65f

        val isTop = centerY < screenHeight * 0.35f
        val isBottom = centerY > screenHeight * 0.65f

        return when {
            isTop && isLeft -> HudAnchor.TOP_LEFT
            isTop && isRight -> HudAnchor.TOP_RIGHT
            isTop -> HudAnchor.TOP_CENTER

            isBottom && isLeft -> HudAnchor.BOTTOM_LEFT
            isBottom && isRight -> HudAnchor.BOTTOM_RIGHT
            isBottom -> HudAnchor.BOTTOM_CENTER

            isLeft -> HudAnchor.MIDDLE_LEFT
            isRight -> HudAnchor.MIDDLE_RIGHT
            else -> HudAnchor.CENTER
        }
    }
}
