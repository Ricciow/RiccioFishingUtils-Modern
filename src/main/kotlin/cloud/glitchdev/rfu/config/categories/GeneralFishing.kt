package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.feature.mob.LootshareRange.RARE_SC_REGEX
import cloud.glitchdev.rfu.access.ConfigScreenInvoker
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.builders.SeparatorBuilder

object GeneralFishing : CategoryKt("General Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for all kinds of fishing!")

    var rareSC by observable(draggable(*SeaCreatures.entries.filter { it.special }.toTypedArray()) {
        name = Literal("Rare Sea Creatures")
        description = Literal("Select which sea creatures are considered rare for the mod.")
    }) { _, new ->
        RARE_SC_REGEX = new.joinToString("|").toRegex()
    }

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

    var RARE_DROP_REGEX : Regex = """RARE DROP! (.+) \(\+(\d+) ✯ Magic Find\)""".toExactRegex()
    var DYE_REGEX : Regex = """WOW! (.+) found a (.+)!""".toExactRegex()
    var rareDrops by observable(draggable(*RareDrops.entries.toTypedArray()) {
        name = Literal("Rare Drops")
        description = Literal("Select which drops are considered rare for the mod.")
    }) { _, new ->
        //RARE DROP! item (+xxx ✯ Magic Find)
        RARE_DROP_REGEX = buildString {
            append("RARE DROP! (")
            append(new.filter { !it.isDye }.joinToString("|"))
            append(""") \(\+(\d+) ✯ Magic Find\)""")
        }.toExactRegex()
        //WOW! [RANK] user found a DyeName!
        DYE_REGEX = buildString {
            append("WOW! (.+) found a (")
            append(new.filter { it.isDye }.joinToString("|"))
            append(")!")
        }.toExactRegex()
    }

    fun dualSeparator(builder: SeparatorBuilder.() -> Unit) {
        separator {}
        separator(builder)
    }
}