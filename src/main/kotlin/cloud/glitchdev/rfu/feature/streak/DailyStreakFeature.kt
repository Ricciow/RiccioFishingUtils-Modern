package cloud.glitchdev.rfu.feature.streak

import cloud.glitchdev.rfu.config.categories.DailyStreakSettings
import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World

@RFUFeature
object DailyStreakFeature : Feature {
    var wasInSkyblock = false

    override fun onInitialize(scope: EventScope) {
        scope.registerJoinEvent(delayMillis = 1000) { _ ->
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

        scope.registerDisconnectEvent {
            wasInSkyblock = false
        }

        scope.registerTickEvent(interval = 20) {
            if (!DailyStreakSettings.dailyStreakEnabled) return@registerTickEvent

            DailyStreakManager.checkDailyReset()
        }

    }
}
