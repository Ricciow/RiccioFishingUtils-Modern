package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.config.SoundTitleSettings
import cloud.glitchdev.rfu.config.TitleSettings
import cloud.glitchdev.rfu.constants.fishing.HotspotType

object HotSpotSettings : Category("Hot Spots") {
    init {
        dualSeparator {
            title = "Visuals"
            description = "Make hotspots easier to see!"
        }
    }

    var highlightHotSpots by reloadableBoolean(true) {
        name = Literal("Highlight Hot Spots")
        description = Literal("Makes hotspots highlighted and hides the particles")
    }

    var hotspotBorderOpacity by int(100) {
        name = Literal("Border Opacity")
        description = Literal("The opacity of the hotspot highlight border")
        range = 0..255
        slider = true
        condition = { highlightHotSpots }
    }

    var hotspotPointer by reloadableBoolean(false) {
        name = Literal("Hotspot Pointer")
        description = Literal(
            "Points a line to the best hotspot when hotspot fishing.\n" +
                "Only points to hotspots that have had their HOTSPOT text previously seen on screen or had their coordinates shared.\n" +
                "Should be fine, but §cuse at your own risk!")
    }

    var hotspotPointerPriority by draggable(
        HotspotType.DOUBLE_HOOK,
        HotspotType.SEA_CREATURE,
        HotspotType.FISHING_SPEED,
        HotspotType.TROPHY,
        HotspotType.TREASURE,
        HotspotType.SHARD
    ) {
        name = Literal("Pointer Priority")
        description = Literal("Drag to reorder the priority for the hotspot pointer.")
        condition = { hotspotPointer }
    }

    init {
        dualSeparator {
            title = "Alerts"
            description = "Be notified about hotspot events"
        }
    }

    val noTikiMask by obj(TitleSettings(
        title = "&c&lNO TIKI MASK!"
    )) {
        name = Literal("No Tiki Mask Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("no tiki mask", "notikimask", "title", "subtitle", "duration")
    }

    val hotspotExpired by obj(SoundTitleSettings(
        title = "&6&lHotspot Expired!",
        soundId = "rfu:hotspot_expired"
    )) {
        name = Literal("Hotspot Expiry Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("hotspot expiry", "hotspotexpired", "title", "subtitle", "duration", "sound", "volume")
    }

    init {
        dualSeparator {
            title = "Sharing"
            description = "Share hotspots with your party!"
        }
    }

    var shareHotspotAlert by reloadableBoolean(true) {
        name = Literal("Share Hotspot Alert")
        description = Literal("Sends a clickable chat message to share the hotspot with your party!")
    }

    var autoShareHotspot by boolean(false) {
        name = Literal("Auto Share Hotspot")
        description = Literal("Automatically shares the hotspot with your party when you're nearby!")
        condition = { shareHotspotAlert }
    }

    var shareHotspotKey by key(0) {
        name = Literal("Share Hotspot Keybind")
        description = Literal("Keybind to manually share the nearest hotspot with your party.")
    }
}
