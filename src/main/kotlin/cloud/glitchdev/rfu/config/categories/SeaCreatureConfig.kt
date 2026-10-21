package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.config.SoundTitleSettings
import cloud.glitchdev.rfu.config.TitleSettings
import cloud.glitchdev.rfu.constants.fishing.RareScDisplayDataType
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
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
                //~ if >=26.2 'screen' -> 'gui.screen()' {
                val window = SeaCreatureEditWindow(mc.gui.screen() as? ConfigScreen)
                //~}
                //~ if >=26.2 'setScreen' -> 'gui.setScreen' {
                mc.gui.setScreen(window)
                //~}
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
        description = Literal("Makes rare sea creatures glow.\n§cUse at your own risk!")
    }

    var timeToKill by boolean(true) {
        name = Literal("Time to kill")
        description = Literal("Sends a message after killing a rare Sea Creature saying how long it took.")
    }

    var killTimePersonalBest by boolean(true) {
        name = Literal("Kill Time PB")
        description = Literal("Sends personal best message for kill times on rare sea creatures.")
    }

    var invulnerabilityTimer by reloadableBoolean(true) {
        name = Literal("Invulnerability Timer")
        description = Literal("Displays the invulnerability timer on rare sea creatures and Vanquishers.")
    }

    init {
        dualSeparator {
            title = "Catch Messages"
            description = "Replace standard catch messages with custom ones"
        }
    }

    var replaceCatchMessages by reloadableBoolean(true) {
        name = Literal("Replace Catch Messages")
        description = Literal("Replaces standard catch messages with custom ones")
    }

    var catchMessageTemplate by string("&3&lSEA CREATURE! &eYou caught {article} {style}&l{name}") {
        name = Literal("Catch Message Template")
        description = Literal("The template for the catch message. Available: {article}, {article_upper}, {name}, {style}, {color}, {plural}, {mob}, {mobs}")
        condition = { replaceCatchMessages }
    }

    var doubleHookCatchMessageTemplate by string("&9&lDOUBLE HOOK! &eYou caught two {style}&l{plural}") {
        name = Literal("Double Hook Message Template")
        description = Literal("The template for the double hook catch message. Available: {article}, {article_upper}, {name}, {style}, {color}, {plural}, {mob}, {mobs}")
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

    val rareSc by obj(SoundTitleSettings(
        title = "&6&l[&fα&6&l] {color}&l{name} &6&l[&fα&6&l]",
        subtitle = "",
        placeholders = "{article}, {article_upper}, {name}, {style}, {color}, {plural}, {mob}, {mobs}",
        previews = mapOf(
            "Preview" to listOf("article" to "a", "article_upper" to "A", "name" to "Lord Jawbus", "style" to "&c", "color" to "&c", "plural" to "Lord Jawbuses", "mob" to "a Lord Jawbus", "mobs" to "Lord Jawbuses")
        ),
        soundId = "rfu:rare_sc"
    )) {
        name = Literal("Rare Sea Creature Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("rare sea creature", "raresc", "title", "subtitle", "duration", "sound", "volume")
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

    val goldenDragon by obj(GoldenDragonTitleSettings()) {
        name = Literal("Golden Dragon Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("golden dragon", "goldendragon", "title", "subtitle", "duration", "sound", "volume", "gdrag", "pet warning", "health threshold", "gdrag alert threshold", "low health")
    }

    init {
        dualSeparator {
            title = "Messages"
            description = "Customize messages sent when catching rare SCs"
        }
    }

    var rarePartyMessages by reloadableBoolean(true) {
        name = Literal("Party SC messages")
        description = Literal("Sends a party message whenever you catch a rare sea creature.")
    }

    var rarePartyMessage by string("WOAH! A {name} just surfaced! {dh}Catch #{count} after {time}!") {
        name = Literal("Rare SC message")
        description = Literal("Variables: {name} {total} {count}, {time}, {dh}, {coords}")
        condition = { rarePartyMessages }
    }

    init {
        previewButton(
            RareScPartyMessage::preview,
            "Preview Message",
            "Shows a preview of the rare SC party message in chat."
        ) { rarePartyMessages }
    }

    var rareCocoonPartyMessages by reloadableBoolean(true) {
        name = Literal("Party Cocoon messages")
        description = Literal("Sends a party message whenever you cocoon a rare sea creature.")
    }

    var rareCocoonPartyMessage by string("WOAH! A {name} was cocooned!") {
        name = Literal("Rare Cocoon message")
        description = Literal("Variables: {name} {total}, {coords}")
        condition = { rareCocoonPartyMessages }
    }

    init {
        previewButton(
            RareScPartyMessage::previewCocoon,
            "Preview Cocoon Message",
            "Shows a preview of the rare cocoon party message in chat."
        ) { rareCocoonPartyMessages }
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

    var bossHealthBars by reloadableBoolean(true) {
        name = Literal("Boss Health Bars")
        description = Literal("Enable health bars that appear when there's a rare mob in sight")
    }

    val HEALTH_BAR_REGEX
        get() = SeaCreatures.entries.filter { it.special && it.bossbar }.joinToString("|") { it.scName }.toExactRegex()

    var coloredShurikenBar by boolean(true) {
        name = Literal("Blue bar on shuriken")
        description = Literal("Makes the health bar blue whenever the mob is shurikened.")
        condition = { bossHealthBars }
    }

    init {
        dualSeparator {
            title = "Rare SC Display"
            description = "Track your rare sea creatures!"
        }
    }

    var rareScDisplay by reloadableBoolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Rare SC display")
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

    init {
        dualSeparator {
            title = "Barn Fishing"
            description = "Features for barn fishers, please don't barn outside of crystal hollows :sob:"
        }
    }

    var scCapDisplay by reloadableBoolean(false) {
        name = Literal("Sea Creature Cap Display")
        description = Literal("Displays the number of active sea creatures owned by you.")
    }

    class GoldenDragonTitleSettings : SoundTitleSettings(
        title = "&c&lNO G-DRAGON!",
        subtitle = "&eEquip your Golden Dragon!",
        duration = 40,
        soundId = "rfu:gdrag_alert"
    ) {
        init {
            separator {
                title = "Trigger Options"
                condition = { enabled }
            }
        }

        var threshold by int(20) {
            name = Literal("GDrag Alert Threshold (%)")
            description = Literal("The health percentage at which the alert will trigger.")
            range = 1..100
            slider = true
            condition = { enabled }
        }
    }
}
