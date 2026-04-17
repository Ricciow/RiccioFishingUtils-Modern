package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.RareScDisplayDataType
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.feature.fishing.CatchMessageReplacer
import cloud.glitchdev.rfu.feature.fishing.RareScPartyMessage
import cloud.glitchdev.rfu.gui.window.SeaCreatureEditWindow
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfig.client.ConfigScreen

object SeaCreatureConfig : Category("Sea Creatures") {
    override val description: TranslatableValue
        get() = Literal("Settings for all your sea creature needs!")

    init {
        dualSeparator {
            title = "General"
            description = "Basic settings for Rare Sea Creatures"
        }

        customButton(
            {
                @Suppress("UnstableApiUsage")
                val window = SeaCreatureEditWindow(mc.screen as? ConfigScreen)
                mc.setScreen(window)
            },
            "Edit Sea Creatures",
            "Open a window to edit sea creature properties (Name, Plural, Article, Special, etc.)",
            "Edit"
        )
    }

    val RARE_SC_REGEX
        get() = SeaCreatures.entries.filter { it.special }.joinToString("|") { it.scName }.toExactRegex()

    var rareScGlow by boolean(false) {
        name = Literal("Rare SC Glow")
        description = Literal("Makes rare sea creatures glow.")
    }

    var timeToKill by boolean(true) {
        name = Literal("Time to kill")
        description = Literal("Sends a message after killing a rare Sea Creature saying how long it took.")
    }

    init {
        dualSeparator {
            title = "Catch Messages"
            description = "Replace standard catch messages with custom ones"
        }
    }

    var replaceCatchMessages by observable(boolean(true) {
        name = Literal("Replace Catch Messages")
        description = Literal("Replaces standard catch messages with custom ones")
    }) { _, _ ->
        reloadScreen()
    }

    var catchMessageTemplate by string("&3&lSEA CREATURE! &eYou caught {article} {style}&l{name}") {
        name = Literal("Catch Message Template")
        description = Literal("The template for the catch message. Available: {article}, {article_upper}, {name}, {style}, {plural}, {mob}, {mobs}")
        condition = { replaceCatchMessages }
    }

    var doubleHookCatchMessageTemplate by string("&9&lDOUBLE HOOK! &eYou caught two {style}&l{plural}") {
        name = Literal("Double Hook Message Template")
        description = Literal("The template for the double hook catch message. Available: {article}, {article_upper}, {name}, {style}, {plural}, {mob}, {mobs}")
        condition = { replaceCatchMessages }
    }

    init {
        previewButton(
            CatchMessageReplacer::preview,
            "Preview Message",
            "Shows a preview of one of the catch messages in chat."
        ) { replaceCatchMessages }
    }

    init {
        dualSeparator {
            title = "Alerts"
            description = "Be notified when a rare SC is found!"
        }
    }

    var detectionAlert by observable(boolean(false) {
        name = Literal("Rare Sc Alert")
        description = Literal("Sends an alert whenever a rare SC is found.")
    }) { _, _ ->
        reloadScreen()
    }

    var rareScSound by observable(boolean(true) {
        name = Literal("Rare Sc Sound")
        description = Literal("Plays a sound whenever a rare SC is found.")
        condition = { detectionAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var rareScSoundVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the rare SC sound")
        range = 0f..1f
        slider = true
        condition = { detectionAlert && rareScSound }
    }

    init {
        dualSeparator {
            title = "Lootshare"
            description = "Settings for lootshare range rendering"
        }
    }

    var lootshareRange by boolean(true) {
        name = Literal("Lootshare Range")
        description = Literal("Shows a sphere around rare sea creatures to display their lootshare range")
    }

    var filledLsRange by boolean(true) {
        name = Literal("Filled lootshare range")
        description = Literal("Renders the lootshare range as a filled sphere")
    }

    init {
        dualSeparator {
            title = "Golden Dragon"
            description = "Alerts for when you're not using a Golden Dragon"
        }
    }

    var goldenDragonAlert by observable(boolean(true) {
        name = Literal("Golden Dragon Alert")
        description = Literal("Sends an alert when a rare SC is low health and you don't have Golden Dragon equipped.")
    }) { _, _ ->
        reloadScreen()
    }

    var gdragAlertThreshold by int(20) {
        name = Literal("GDrag Alert Threshold (%)")
        description = Literal("The health percentage at which the alert will trigger.")
        range = 1..100
        slider = true
        condition = { goldenDragonAlert }
    }

    var goldenDragonSound by observable(boolean(true) {
        name = Literal("GDrag Alert Sound")
        description = Literal("Plays a sound when the alert triggers.")
        condition = { goldenDragonAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var goldenDragonVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the GDrag alert sound")
        range = 0f..1f
        slider = true
        condition = { goldenDragonAlert && goldenDragonSound }
    }

    init {
        dualSeparator {
            title = "Messages"
            description = "Customize messages sent when catching rare SCs"
        }
    }

    var rarePartyMessages by observable(boolean(false) {
        name = Literal("Party SC messages")
        description = Literal("Sends a party message whenever you catch a rare sea creature.")
    }) { _, _ ->
        reloadScreen()
    }

    var rarePartyMessage by string("WOAH! A {name} just surfaced! {dh}Catch #{count} after {time}!") {
        name = Literal("Rare SC message")
        description = Literal("Variables: {name} {total} {count}, {time}, {dh}")
        condition = { rarePartyMessages }
    }

    init {
        previewButton(
            RareScPartyMessage::preview,
            "Preview Message",
            "Shows a preview of the rare SC party message in chat."
        ) { rarePartyMessages }
    }

    var dhText by string("(Double Hook) ") {
        name = Literal("Double Hook Text")
        description = Literal("The text used in {dh}")
        condition = { rarePartyMessages }
    }

    init {
        dualSeparator {
            title = "Health Bars"
            description = ""
        }
    }

    var bossHealthBars by observable(boolean(true) {
        name = Literal("Boss Health Bars")
        description = Literal("Enable health bars that appear when there's a rare mob in sight")
    }) { _, _ ->
        reloadScreen()
    }

    val HEALTH_BAR_REGEX
        get() = SeaCreatures.entries.filter { it.special }.joinToString("|") { it.scName }.toExactRegex()


    var coloredShurikenBar by boolean(true) {
        name = Literal("Blue bar on shuriken")
        description = Literal("Makes the health bar blue whenever the mob is shurikened.")
        condition = { bossHealthBars }
    }

    var boostPollingRate by boolean(true) {
        name = Literal("Boost Polling Rate")
        description = Literal("Makes detections more frequent when the health bar is active (Probably wont but may cause lag, hence the option)")
        condition = { bossHealthBars }
    }

    init {
        dualSeparator {
            title = "Rare SC Display"
            description = "Track your rare sea creatures!"
        }
    }

    var rareScDisplay by observable(boolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Rare SC display")
    }) { _, _ ->
        reloadScreen()
    }

    var rareScOnlyWhenFishing by boolean(true) {
        name = Literal("Only display when fishing")
        description = Literal("Only show the display when you're fishing")
        condition = { rareScDisplay }
    }

    var rareScDisplayDataOrder by draggable(*RareScDisplayDataType.entries.toTypedArray()) {
        name = Literal("Display Data Order")
        description = Literal("Drag to reorder the data shown for each sea creature.")
        condition = { rareScDisplay }
    }
}
