package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object DailyStreakSettings : Category("Daily Streaks") {
    override val description: TranslatableValue
        get() = Literal("Settings for daily fishing streaks and daily challenges.")

    var dailyStreakEnabled by reloadableBoolean(true) {
        name = Literal("Enable Daily Streaks")
        description = Literal("Tracks your daily fishing streak and daily challenges.")
    }

    var dailyStreakHudEnabled by boolean(true) {
        name = Literal("Daily Streak HUD")
        description = Literal("Toggle whether the daily streak hud is displayed")
    }

    var showLoginNotification by boolean(true) {
        name = Literal("Login Streak Notification")
        description = Literal("Sends a subtle summary of your daily streak status in chat when joining.")
        condition = { dailyStreakEnabled }
    }

    var autoHideCompletedHud by boolean(true) {
        name = Literal("Auto-Hide Completed HUD")
        description = Literal("Hides the daily streak HUD element when all daily challenges for today are completed.")
        condition = { dailyStreakEnabled }
    }

    var completionSound by reloadableBoolean(true) {
        name = Literal("Challenge Completion Sound")
        description = Literal("Plays a sound when you complete a daily challenge or daily streak.")
        condition = { dailyStreakEnabled }
    }

    var completionVolume by float(1f) {
        name = Literal("Completion Sound Volume")
        description = Literal("The volume for daily challenge completion sound.")
        range = 0f..1f
        slider = true
        condition = { dailyStreakEnabled && completionSound }
    }
}
