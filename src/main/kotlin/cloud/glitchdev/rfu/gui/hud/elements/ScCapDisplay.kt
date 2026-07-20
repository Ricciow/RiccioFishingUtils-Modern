package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.mob.MobManager
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.gui.hud.AbstractFishingHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import gg.essential.universal.utils.toUnformattedString

@HudElement
object ScCapDisplay : AbstractFishingHudElement("scCapDisplay") {
    override val requirement: Boolean
        get() = SeaCreatureConfig.scCapDisplay

    override fun onInitialize() {
        super.onInitialize()
        registerTickEvent(interval = 20) {
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        val player = mc.player
        val ownedScCount = if (player != null) {
            val playerUuid = player.uuid
            val playerName = player.name.toUnformattedString()
            MobManager.getEntities().count { entity ->
                val origin = entity.originBobber
                origin != null && (origin.ownerUUID == playerUuid || (origin.ownerName != null && origin.ownerName == playerName))
            }
        } else {
            0
        }

        val countToUse = if (isEditing && ownedScCount == 0) 5 else ownedScCount
        val xColor = when {
            countToUse < 6 -> TextColor.LIGHT_GREEN
            countToUse in 6..8 -> TextColor.YELLOW
            else -> TextColor.LIGHT_RED
        }

        text.setText("${TextColor.LIGHT_BLUE}Personal Cap: $xColor$countToUse${TextColor.WHITE}/10")
    }
}
