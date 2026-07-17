package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig.RARE_SC_REGEX
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.CocoonEvents.registerCocoonEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.dsl.formatTemplate
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock

@RFUFeature
object RareScPartyMessage : Feature {
    override fun onInitialize() {
        registerSeaCreatureCatchEvent(-10) { seaCreature, isDoubleHook, _, _, _ ->
            if(!SeaCreatureConfig.rarePartyMessages) return@registerSeaCreatureCatchEvent

            if(RARE_SC_REGEX.matches(seaCreature.scName)) {
                val history = CatchTracker.catchHistory.getOrAdd(seaCreature)
                val timeSinceLast = if (history.total > 0) {
                    (Clock.System.now() - history.time).toReadableString()
                } else {
                    "First Catch"
                }

                val scName = seaCreature.scDisplayName
                val article = if (scName.take(1).lowercase() in "aeiou") "n" else ""
                val startsWithThe = scName.startsWith("The ", ignoreCase = true)
                val pos = mc.player?.blockPosition()

                val template = seaCreature.rarePartyMessage.ifEmpty {
                    SeaCreatureConfig.rarePartyMessage
                }

                val messageString = template
                    .replace("""(?i)\b(a)\s\{name\}""".toRegex()) { match  ->
                        if (startsWithThe) return@replace scName
                        val originalA = match.groupValues[1]
                        "$originalA$article $scName"
                    }
                    .formatTemplate(
                        "name" to scName,
                        "total" to history.total.toString(),
                        "count" to (history.count + 1).toString(),
                        "time" to timeSinceLast,
                        "dh" to if (isDoubleHook) SeaCreatureConfig.dhText else "",
                        "coords" to if (pos != null) "X: ${pos.x}, Y: ${pos.y}, Z: ${pos.z}" else ""
                    )
                Chat.sendPartyMessage(messageString)
            }
        }

        registerCocoonEvent { seaCreature ->
            if (!SeaCreatureConfig.rareCocoonPartyMessages) return@registerCocoonEvent

            if (RARE_SC_REGEX.matches(seaCreature.scName)) {
                val history = CatchTracker.catchHistory.getOrAdd(seaCreature)
                val scName = seaCreature.scDisplayName
                val article = if (scName.take(1).lowercase() in "aeiou") "n" else ""
                val startsWithThe = scName.startsWith("The ", ignoreCase = true)
                val pos = mc.player?.blockPosition()

                val messageString = SeaCreatureConfig.rareCocoonPartyMessage
                    .replace("""(?i)\b(a)\s\{name\}""".toRegex()) { match ->
                        if (startsWithThe) return@replace scName
                        val originalA = match.groupValues[1]
                        "$originalA$article $scName"
                    }
                    .formatTemplate(
                        "name" to scName,
                        "total" to history.total.toString(),
                        "coords" to if (pos != null) "X: ${pos.x}, Y: ${pos.y}, Z: ${pos.z}" else ""
                    )
                Chat.sendPartyMessage(messageString)
            }
        }
    }

    fun preview() {
        val preview = SeaCreatureConfig.rarePartyMessage.formatTemplate(
            "name" to "Great White Shark",
            "total" to "100",
            "count" to "1",
            "time" to "2m 30s",
            "dh" to SeaCreatureConfig.dhText
        )

        Chat.sendPreviewPartyMessage(preview)
    }

    fun previewCocoon() {
        val preview = SeaCreatureConfig.rareCocoonPartyMessage.formatTemplate(
            "name" to "Great White Shark",
            "total" to "100"
        )

        Chat.sendPreviewPartyMessage(preview)
    }
}