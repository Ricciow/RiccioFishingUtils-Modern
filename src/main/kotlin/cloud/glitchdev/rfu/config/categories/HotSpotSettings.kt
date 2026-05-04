package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.HotspotType

object HotSpotSettings : Category("Hot Spots") {
    init {
        dualSeparator {
            title = "Visuals"
            description = "Make hotspots easier to see!"
        }
    }

    var highlightHotSpots by boolean(true) {
        name = Literal("Highlight Hot Spots")
        description = Literal("Makes hotspots highlighted and hides the particles")
    }

    var hotspotPointer by observable(boolean(false) {
        name = Literal("Hotspot Pointer")
        description = Literal(
            "Points a line to the best hotspot when hotspot fishing.\n" +
                "Only points to hotspots that have had their HOTSPOT text previously seen on screen or had their coordinates shared.\n" +
                "Should be fine, but §cuse at your own risk!")
    }) { _, _ -> reloadScreen() }

    var hotspotPointerPriority by draggable(
        HotspotType.DOUBLE_HOOK,
        HotspotType.SEA_CREATURE,
        HotspotType.FISHING_SPEED,
        HotspotType.TROPHY_FISH,
        HotspotType.TREASURE
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

    var hotspotExpiredAlert by boolean(true) {
        name = Literal("Hotspot Expired Alert")
        description = Literal("Sends an alert when the hotspot you're fishing in expires!")
    }

    var hotspotExpiredSound by observable(boolean(true) {
        name = Literal("Hotspot Expired Sound")
        description = Literal("Plays a sound when the hotspot you're fishing in expires!")
        condition = { hotspotExpiredAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var hotspotExpiredVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the hotspot expired sound")
        range = 0f..1f
        slider = true
        condition = { hotspotExpiredAlert && hotspotExpiredSound }
    }

    init {
        dualSeparator {
            title = "Sharing"
            description = "Share hotspots with your party!"
        }
    }

    var shareHotspotAlert by observable(boolean(true) {
        name = Literal("Share Hotspot Alert")
        description = Literal("Sends a clickable chat message to share the hotspot with your party!")
    }) { _, _ ->
        reloadScreen()
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