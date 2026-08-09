package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.DailyStreakSettings
import cloud.glitchdev.rfu.constants.text.TextColor.GOLD
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_GREEN
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.WHITE
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ArmorEvents.registerArmorChangeEvent
import cloud.glitchdev.rfu.events.managers.DailyStreakEvents.registerStreakUpdatedEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement

@HudElement
object DailyStreakHUD : AbstractTextHudElement("dailyStreakDisplay") {
    override val requirement: Boolean
        get() = DailyStreakSettings.dailyStreakEnabled

    override val isElementActive: Boolean
        get() {
            if (!DailyStreakSettings.dailyStreakEnabled) return false
            if (isEditing) return true
            if (!ArmorEvents.currentArmorSet.isWearingFishingArmor) return false
            if (DailyStreakSettings.autoHideCompletedHud) {
                return DailyStreakManager.data.todayChallenges.any { !it.isCompleted }
            }
            return true
        }

    override fun onInitialize() {
        super.onInitialize()
        registerTickEvent(interval = 20) {
            updateState()
        }
        registerStreakUpdatedEvent {
            updateState()
        }
        registerArmorChangeEvent {
            updateState()
        }
    }

    override fun onUpdateState() {
        super.onUpdateState()

        if (!enabled) return

        val data = DailyStreakManager.data
        val lines = mutableListOf<String>()

        lines.add("${WHITE}\uE11F${GOLD}${BOLD}Daily Streak: ${YELLOW}${data.currentStreak} Days")

        if (data.todayChallenges.isEmpty()) {
            if (isEditing) {
                lines.add("${LIGHT_GREEN}✔ Daily Angler: 15/15m")
            }
        } else {
            data.todayChallenges.forEach { challenge ->
                val icon = if (challenge.isCompleted) "${LIGHT_GREEN}✔" else "${LIGHT_RED}✘"
                val target = challenge.getTargetProgress()
                val progressStr = if (challenge.isCompleted) {
                    "${LIGHT_GREEN}${target}/${target}"
                } else {
                    "${YELLOW}${challenge.currentProgress}/${target}"
                }
                lines.add("$icon ${GOLD}${challenge.getTitle()}: $progressStr")
            }
        }

        text.setText(lines.joinToString("\n"))
    }
}
