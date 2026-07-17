package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.dsl.formatTemplate

@RFUFeature
object VanquisherPartyMessage : Feature {
    private var seaCreaturesSinceLastVanquisher = 0

    override fun onInitialize() {
        registerSeaCreatureCatchEvent { seaCreature, isDoubleHook, _, _, _ ->
            if (seaCreature.category.islands.contains(FishingIslands.ISLE)) {
                seaCreaturesSinceLastVanquisher += if (isDoubleHook) 2 else 1
            }
        }

        registerGameEvent(Regex("A Vanquisher is spawning nearby!")) { _, _, _ ->
            if (!LavaFishing.vanquisherPartyMessages) return@registerGameEvent

            val pos = mc.player?.blockPosition()
            val coordsStr = if (pos != null) "X: ${pos.x}, Y: ${pos.y}, Z: ${pos.z}" else ""
            val messageTemplate = if (FishingSession.isFishing) {
                LavaFishing.vanquisherPartyMessageFishing
            } else {
                LavaFishing.vanquisherPartyMessageNoFishing
            }
            val messageString = messageTemplate.formatTemplate(
                "count" to seaCreaturesSinceLastVanquisher.toString(),
                "coords" to coordsStr
            )
            Chat.sendPartyMessage(messageString)
            seaCreaturesSinceLastVanquisher = 0
        }
    }

    fun previewNoFishing() {
        val pos = mc.player?.blockPosition()
        val coordsStr = if (pos != null) "X: ${pos.x}, Y: ${pos.y}, Z: ${pos.z}" else "X: 10, Y: 70, Z: -20"
        val preview = LavaFishing.vanquisherPartyMessageNoFishing.formatTemplate(
            "coords" to coordsStr,
            "count" to "0"
        )
        Chat.sendPreviewPartyMessage(preview)
    }

    fun previewFishing() {
        val pos = mc.player?.blockPosition()
        val coordsStr = if (pos != null) "X: ${pos.x}, Y: ${pos.y}, Z: ${pos.z}" else "X: 10, Y: 70, Z: -20"
        val preview = LavaFishing.vanquisherPartyMessageFishing.formatTemplate(
            "coords" to coordsStr,
            "count" to "42"
        )
        Chat.sendPreviewPartyMessage(preview)
    }
}
