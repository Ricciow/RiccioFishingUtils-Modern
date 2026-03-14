package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object RareScSettings : Category("Rare SCs") {
    override val description: TranslatableValue
        get() = Literal("Settings for your great catches!")

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

    var filledLsRange by boolean(true) {
        name = Literal("Filled lootshare range")
        description = Literal("Renders the lootshare range as a filled sphere")
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

    var timeToKill by boolean(true) {
        name = Literal("Time to kill")
        description = Literal("Sends a message after killing a rare Sea Creature saying how long it took.")
    }

    init {
        dualSeparator {
            title = "Messages"
            description = ""
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

    var healthBarMobs by draggable(*SeaCreatures.entries.filter { it.special }.toTypedArray()) {
        name = Literal("Boss Health Bar mobs")
        description = Literal("Select which mobs will have their health bars displayed")
        condition = { bossHealthBars }
    }

    val HEALTH_BAR_REGEX
        get() = healthBarMobs.joinToString("|").toExactRegex()


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
}