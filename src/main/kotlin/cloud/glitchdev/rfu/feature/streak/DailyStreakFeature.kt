package cloud.glitchdev.rfu.feature.streak

import cloud.glitchdev.rfu.config.categories.DailyStreakSettings
import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import net.minecraft.network.chat.Component

@RFUFeature
object DailyStreakFeature : Feature {
    var wasInSkyblock = false

    override fun onInitialize() {
        registerJoinEvent(delayMillis = 1000) { _ ->
            if (!DailyStreakSettings.dailyStreakEnabled || !World.isOnHypixel || !World.isInSkyblock) return@registerJoinEvent

            DailyStreakManager.checkDailyReset()
            if (!wasInSkyblock && World.isInSkyblock) {
                if (DailyStreakSettings.showLoginNotification) {
                    val data = DailyStreakManager.data
                    val completedCount = data.todayChallenges.count { it.isCompleted }
                    val totalCount = data.todayChallenges.size
                    Chat.sendMessage(TextUtils.rfuLiteral("").append(Emoji.icon("fire").component())
                        .append("§6 Daily Streak: Day ${data.currentStreak} §7(${completedCount}/${totalCount} done) §e[/rfudailies]"))
                }
            }
            wasInSkyblock = World.isInSkyblock
        }

        registerDisconnectEvent {
            wasInSkyblock = false
        }

        registerTickEvent(interval = 20) {
            if (!DailyStreakSettings.dailyStreakEnabled) return@registerTickEvent

            DailyStreakManager.checkDailyReset()
        }

    }
}
