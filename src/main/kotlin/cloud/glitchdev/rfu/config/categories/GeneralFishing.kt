package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.utils.dsl.escapeForRegex
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object GeneralFishing : Category("General Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for all kinds of fishing!")

    init {
        dualSeparator {
            title = "Rare Sea Creatures"
            description  = ""
        }
    }

    var rareSC by draggable(*SeaCreatures.entries.filter { it.special }.toTypedArray()) {
        name = Literal("Rare Sea Creatures")
        description = Literal("Select which sea creatures are considered rare for the mod.")
    }

    val RARE_SC_REGEX
        get() = rareSC.joinToString("|").toExactRegex()

    var lootshareRange by boolean(true) {
        name = Literal("Lootshare Range")
        description = Literal("Shows a sphere around rare sea creatures to display their lootshare range")
    }

    var detectionAlert by boolean(false) {
        name = Literal("Rare Sc Alert")
        description = Literal("Sends an alert whenever a rare SC is found.")
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

    var dhText by string("(Double Hook) ") {
        name = Literal("Double Hook Text")
        description = Literal("The text used in {dh}")
        condition = { rarePartyMessages }
    }

    var bossHealthBars by observable(boolean(true) {
        name = Literal("Boss Health Bars")
        description = Literal("Enable health bars that appear when there's a rare mob in sight")
    }) { _, _ ->
        reloadScreen()
    }

    var healthBarMobs by draggable(*SeaCreatures.entries.filter { it.special }.toTypedArray()) {
        name = Literal("Boss Health Bar mobs")
        description = Literal("Select which mobs will have their health bars displayed")
        condition = { bossHealthBars }
    }

    val HEALTH_BAR_REGEX
        get() = healthBarMobs.joinToString("|").toExactRegex()

    var boostPollingRate by boolean(true) {
        name = Literal("Boost Polling Rate")
        description = Literal("Makes detections more frequent when the health bar is active (Probably wont but may cause lag, hence the option)")
        condition = { bossHealthBars }
    }

    var coloredShurikenBar by boolean(true) {
        name = Literal("Blue bar on shuriken")
        description = Literal("Makes the health bar blue whenever the mob is shurikened.")
        condition = { bossHealthBars }
    }

    init {
        dualSeparator {
            title = "SC/h Display"
            description = "Shows how many sea creatures you've caught on average since you start fishing"
        }
    }

    var schDisplay by observable(boolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Sc/h display")
    }) { _, _ ->
        reloadScreen()
    }

    var schTimer by boolean(true) {
        name = Literal("Toggle Timer")
        description = Literal("Shows for how long you've been fishing alongside the sc/h")
        condition = { schDisplay }
    }

    var schOverall by boolean(false) {
        name = Literal("Add overall text")
        description = Literal("Shows your sc/h overall alongside current")
        condition = { schDisplay }
    }

    var schOnlyWhenFishing by boolean(true) {
        name = Literal("Only display when fishing")
        description = Literal("Only show the sch display when you're fishing")
        condition = { schDisplay }
    }

    var fishingTime by int(5) {
        name = Literal("Fishing Downtime Limit")
        description = Literal("The max ammount of downtime for the sc/h and xp/h counters to reset in minutes, also used as the window (e.g. 5 -> sc/h during last 5 minutes)")
        condition = { schDisplay || xphDisplay }
        range = 0..60
        slider = true
    }


    init {
        dualSeparator {
            title = "Fishing Xp/h Display"
            description = "Shows how much fishing XP you gain per hour"
        }
    }

    var xphDisplay by observable(boolean(true) {
        name = Literal("Toggle")
        description = Literal("Enables the Xp/h display")
    }) { _, _ ->
        reloadScreen()
    }

    var xphTimer by boolean(false) {
        name = Literal("Toggle Timer")
        description = Literal("Shows for how long you've been fishing alongside the xp/h")
        condition = { xphDisplay }
    }

    var xphOverall by boolean(false) {
        name = Literal("Add overall text")
        description = Literal("Shows your xp/h overall alongside current")
        condition = { xphDisplay }
    }

    var xphOnlyWhenFishing by boolean(true) {
        name = Literal("Only display when fishing")
        description = Literal("Only show the xp/h display when you're fishing")
        condition = { xphDisplay }
    }


    init {
        dualSeparator {
            title = "Rare Drops Tracking"
            description = "Track your rare drops when you fish!"
        }
    }

    var rareDrops by draggable(*RareDrops.entries.toTypedArray()) {
        name = Literal("Rare Drops")
        description = Literal("Select which drops are considered rare for the mod.")
    }

    var dyeDrops by draggable(*Dyes.entries.toTypedArray()) {
        name = Literal("Dye drops")
        description = Literal("Select which dyes are considered rare for the mod.")
    }

    val RARE_DROP_REGEX : Regex
        get() = buildString {
            append("RARE DROP! (")
            append(rareDrops.joinToString("|") { it.overrideRegex ?: it.toString().escapeForRegex() })
            append(""") \(\+(\d+) ✯ Magic Find\)""")
        }.toExactRegex()
    val DYE_REGEX : Regex
        get() = buildString {
            append("WOW! (.+) found a (")
            append(dyeDrops.joinToString("|") { it.toString().escapeForRegex() })
            append(")!")
        }.toExactRegex()


    var customRareDropMessage by observable(boolean(false) {
        name = Literal("Enable Custom Rare Drop Message")
        description = Literal("Shows a custom message when you get a rare drop")
    }) { _, _ ->
        reloadScreen()
    }

    var rareDropMessageFormat by string("&6&lRARE DROP! &e{drop} &b(+{magic_find}% ✯ Magic Find) &7(Took {count} catches, {time} since last)") {
        name = Literal("Custom Message Format")
        description = Literal("Variables: {drop}, {magic_find}, {count}, {time}")
        condition = { customRareDropMessage }
    }

    var rareDropPartyChat by boolean(true) {
        name = Literal("Send in party chat")
        description = Literal("Sends the drop message in party chat, uses the same message as above but removes the colors")
        condition = { customRareDropMessage }
    }

    init {
        dualSeparator {
            title = "Deployables"
            description = "Everything deployables related, Flares, Fluxes, You name it!"
        }
    }

    var flareTimerDisplay by boolean(true) {
        name = Literal("Flare Timer")
        description = Literal("Enables the Flare Timer display")
    }

    var flareAlert by boolean(true) {
        name = Literal("Expiration Alert")
        description = Literal("Sends an alert in your screen when the flare expires.")
    }

    init {
        dualSeparator {
            title = "Double Hook"
            description = "Double hook shenanigans"
        }
    }

    var toggleDoubleHookMessages by observable(boolean(false) {
        name = Literal("Toggle Double Hook Messages")
        description = Literal("Automatically send messages when you get a double hook!")
    }) { _, _ ->
        reloadScreen()
    }

    var doubleHookMessages by strings(
        "o/ &9~~~~~~~|&f_&9|",
        "o| &9~~~~~~~&c.&9~",
        "o| &9~~~~~~~&c*&9~",
        "o| &9~~~~~~&3<><",
        "o| &9~~~~&3<><&9~~",
        "o| &9~~&3<><&9~~~~",
        "\\o/ &3<><&9~~~~~",
        "( ^_^) &b[ &3<>< &b]",
        "( >_<) &b[ &3RFU &b]"
    ) {
        name = Literal("Double Hook messages")
        description = Literal("Select what words will be sent when you get a double hook. Each line is one phrase.")
        condition = { toggleDoubleHookMessages }
    }

    var randomDoubleHookMessages by boolean(false) {
        name = Literal("Random Double Hook Messages")
        description = Literal("Makes double hook messages random")
        condition = { toggleDoubleHookMessages }
    }

    init {
        dualSeparator {
            title = "Fishing"
            description = "Anything fishing related that didn't fit elsewhere"
        }
    }

    var rodTimerDisplay by boolean(false) {
        name = Literal("Rod Timer Display")
        description = Literal("Display the current rod timer on screen")
    }

    var failCastAlert by boolean(true) {
        name = Literal("Failed cast alert")
        description = Literal("Sends an alert whenever a rod cast fails.")
    }
}