package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.config.categories.RareScSettings.detectionAlert
import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.data.mob.DeployableType
import cloud.glitchdev.rfu.utils.dsl.escapeForRegex
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object GeneralFishing : Category("General Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for all kinds of fishing!")

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
            append(""")(?: \(\+(\d+) ✯ Magic Find\))?""")
        }.toExactRegex()
    val DYE_REGEX : Regex
        get() = buildString {
            append("WOW! (.+) found (?:an? )?(")
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

    var lootshareMessage by boolean(true) {
        name = Literal("Lootshare Message")
        description = Literal("Sends a message when lootshare gives you an item.")
    }

    init {
        dualSeparator {
            title = "Deployables"
            description = "Everything deployables related, Flares, Fluxes, You name it!"
        }
    }

    var deployableDisplay by observable(boolean(true) {
        name = Literal("Deployable Display")
        description = Literal("Toggles the deployable display")
    }) { _, _ ->
        reloadScreen()
    }

    var deployableTimerDisplay by enums(*DeployableType.entries.toTypedArray()) {
        name = Literal("Deployable Timers")
        description = Literal("Select which deployable timers to display.")
        condition = { deployableDisplay }
    }

    var deployableExpiredAlert by observable(boolean(true) {
        name = Literal("Deployable Display")
        description = Literal("Toggles the deployable display")
    }) { _, _ ->
        reloadScreen()
    }

    var deployableAlertTypes by enums(*DeployableType.entries.toTypedArray()) {
        name = Literal("Deployable Alerts")
        description = Literal("Select which deployable will cause an alert.")
        condition = { deployableDisplay }
    }

    var deployableExpiredSound by observable(boolean(true) {
        name = Literal("Expired Sound")
        description = Literal("Plays a sound whenever a deployable expires.")
        condition = { detectionAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var deployableExpiredVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the expired sound")
        range = 0f..1f
        slider = true
        condition = { deployableExpiredAlert && deployableExpiredSound }
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

    var failCastSound by observable(boolean(false) {
        name = Literal("Expired Sound")
        description = Literal("Plays a sound whenever a deployable expires.")
        condition = { failCastAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var failCastVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the expired sound")
        range = 0f..1f
        slider = true
        condition = { failCastAlert && failCastSound }
    }
}