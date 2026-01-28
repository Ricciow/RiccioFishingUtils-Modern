package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.access.ConfigScreenInvoker
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.builders.SeparatorBuilder

object GeneralFishing : CategoryKt("General Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for all kinds of fishing!")

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
        val screen = minecraft.currentScreen as? ConfigScreenInvoker
        screen?.rfuInvokeClearAndInit()
    }

    var schTimer by boolean(true) {
        name = Literal("Toggle Timer")
        description = Literal("Shows for how long you've been fishing alongside the sc/h")
        condition = { schDisplay }
    }

    var schOnlyWhenFishing by boolean(true) {
        name = Literal("Only display when fishing")
        description = Literal("Only show the sch display when you're fishing")
        condition = { schDisplay }
    }

    var fishingTime by int(5) {
        name = Literal("Fishing Downtime Limit")
        description = Literal("The max ammount of downtime for the sc/h counter to reset in minutes")
        condition = { schDisplay }
        range = 0..60
        slider = true
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

    val RARE_DROP_REGEX : Regex
        get() = buildString {
            append("RARE DROP! (")
            append(rareDrops.filter { !it.isDye }.joinToString("|") { Regex.escape(it.toString()) })
            append(""") \(\+(\d+) ✯ Magic Find\)""")
        }.toExactRegex()
    val DYE_REGEX : Regex
        get() = buildString {
            append("WOW! (.+) found a (")
            append(rareDrops.filter { it.isDye }.joinToString("|") { Regex.escape(it.toString()) })
            append(")!")
        }.toExactRegex()


    init {
        dualSeparator {
            title = "Custom Messages"
            description = "Customize the chat messages for rare drops"
        }
    }

    var customRareDropMessage by boolean(false) {
        name = Literal("Enable Custom Rare Drop Message")
        description = Literal("Shows a custom message when you get a rare drop")
    }

    var rareDropMessageFormat by string("&6&lRARE DROP! &e{drop} &b(+{magic_find}% ✯ Magic Find) &7(Took {count} catches, {time} since last)") {
        name = Literal("Custom Message Format")
        description = Literal("Variables: {drop}, {magic_find}, {count}, {time}")
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

    fun dualSeparator(builder: SeparatorBuilder.() -> Unit) {
        separator {}
        separator(builder)
    }
}