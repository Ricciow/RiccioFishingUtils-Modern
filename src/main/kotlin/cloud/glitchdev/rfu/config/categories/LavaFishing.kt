package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.config.SoundTitleSettings
import cloud.glitchdev.rfu.constants.fishing.LiquidTypes
import cloud.glitchdev.rfu.feature.fishing.VanquisherPartyMessage
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import java.awt.Color

object LavaFishing : Category("Lava Fishing") {
    init {
        dualSeparator {
            title = "Jawbus"
            description = "Settings for the final boss."
        }
    }

    var jawbus_hard_mode by boolean(false) {
        name = Literal("Jawbus hard mode")
        description = Literal("Pro hint: Don't die")
    }

    var jawbusInvulnerabilityTimer by boolean(true) {
        name = Literal("Jawbus Invulnerability Timer")
        description = Literal("Tracks Jawbus' invulnerability in its phases.")
    }

    val jawbusDeath by obj(SoundTitleSettings(
        title = "&8☠ &7{player} &8☠",
        subtitle = "&cwas killed by Lord Jawbus",
        placeholders = "{player}",
        previews = mapOf(
            "Preview" to listOf("player" to "Ricciow")
        ),
        soundId = "rfu:jawbus_death"
    )) {
        name = Literal("Jawbus Death Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("jawbus death", "jawbusdeath", "title", "subtitle", "duration", "sound", "volume")
    }

    init {
        dualSeparator {
            title = "Plhlegblast"
            description = "Settings for the elusive Plhlegblast"
        }
    }

    var plhlegblastGlow by boolean(true) {
        name = Literal("Plhlegblast Glow")
        description = Literal("Visually transforms Squids named 'Plhlegblast' into Glow Squids")
    }

    init {
        dualSeparator {
            title = "Vanquishers"
            description = "Settings for Vanquishers"
        }
    }

    var vanquisherPartyMessages by reloadableBoolean(true) {
        name = Literal("Vanquisher Party Message")
        description = Literal("Sends a party message when a Vanquisher spawns.")
    }

    var vanquisherPartyMessageNoFishing by string("A Vanquisher has spawned! {coords}") {
        name = Literal("Vanquisher Message (Not Fishing)")
        description = Literal("The message sent when a Vanquisher spawns and you are not fishing. Params: {count}, {coords}")
        condition = { vanquisherPartyMessages }
    }

    var vanquisherPartyMessageFishing by string("A Vanquisher has spawned! #{count} SCs") {
        name = Literal("Vanquisher Message (Fishing)")
        description = Literal("The message sent when a Vanquisher spawns and you are fishing. Params: {count}, {coords}")
        condition = { vanquisherPartyMessages }
    }

    init {
        previewButton(
            VanquisherPartyMessage::previewNoFishing,
            "Preview Message (Not Fishing)",
            "Shows a preview of the vanquisher party message when not fishing in chat."
        ) { vanquisherPartyMessages }

        previewButton(
            VanquisherPartyMessage::previewFishing,
            "Preview Message (Fishing)",
            "Shows a preview of the vanquisher party message when fishing in chat."
        ) { vanquisherPartyMessages }
    }

    init {
        dualSeparator {
            title = "Other"
            description = "Other stuff!"
        }
    }

    var highlightItemsToSell by boolean(false) {
        name = Literal("Highlight Items to Sell")
        description = Literal("Highlights the random lava stuff while trades is open.")
    }

    var showCrimsonIsleWorldBorder by boolean(true) {
        name = Literal("Show Crimson Isle World Border")
        description = Literal("Shows a world border on the Crimson Isle, only on the spider's den side.")
    }

    init {
        dualSeparator {
            title = "Custom Lava"
            description = "Customize the appearance of lava."
        }
    }

    var customLava by reloadableBoolean(false) {
        name = Literal("Custom Lava")
        description = Literal("Changes lava's appearance, color, and fog. Just don't forget it's still lava!")
    }

    var customLavaLiquid by ObservableEntry(enum(LiquidTypes.WATER) {
        name = Literal("Liquid Style")
        description = Literal("Pick the liquid style for the custom lava.")
        condition = { customLava }
    }) { _, _ ->
        reloadScreen()
    }

    var customLavaCustomColor by reloadableBoolean(true) {
        name = Literal("Custom Colors")
        description = Literal("Overrides the liquid's original colors.")
        condition = { customLava }
    }

    var customLavaColor by color(Color(63, 118, 228).rgb) {
        name = Literal("Liquid Color")
        description = Literal("Changes the color of the liquid.")
        allowAlpha = false
        condition = { customLava && customLavaCustomColor && customLavaLiquid == LiquidTypes.WATER }
    }

    var waterTextureOpacity by float(180f / 255f * 100f) {
        name = Literal("Opacity")
        description = Literal("The opacity of the water.")
        range = 0f..100f
        slider = true
        condition = { customLava && customLavaLiquid == LiquidTypes.WATER }
    }

    var customLavaDarkColor by color(Color(155, 23, 0).rgb) {
        name = Literal("Dark Tones")
        description = Literal("Changes the darkest shades of the lava.")
        allowAlpha = false
        condition = { customLava && customLavaCustomColor && customLavaLiquid == LiquidTypes.LAVA }
    }

    var customLavaMidtoneColor by color(Color(255, 100, 0).rgb) {
        name = Literal("Midtones")
        description = Literal("Changes the middle shades and fog of the lava.")
        allowAlpha = false
        condition = { customLava && customLavaCustomColor && customLavaLiquid == LiquidTypes.LAVA }
    }

    var customLavaHighlightColor by color(Color(255, 255, 85).rgb) {
        name = Literal("Highlights")
        description = Literal("Changes the brightest shades of the lava.")
        allowAlpha = false
        condition = { customLava && customLavaCustomColor && customLavaLiquid == LiquidTypes.LAVA }
    }

    var lavaTextureOpacity by float(100f) {
        name = Literal("Opacity")
        description = Literal("The opacity of the lava.")
        range = 0f..100f
        slider = true
        condition = { customLava && customLavaLiquid == LiquidTypes.LAVA }
    }

    var customLavaDisableFog by boolean(false) {
        name = Literal("Disable Fog")
        description = Literal("Removes the fog while inside the lava/water.")
        condition = { customLava }
    }

    var customLavaHideFire by boolean(false) {
        name = Literal("Hide Fire Effect")
        description = Literal("Hides the first-person fire overlay and flames on players/bobbers.")
        condition = { customLava }
    }

}
