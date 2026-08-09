package cloud.glitchdev.rfu.data.streak

import cloud.glitchdev.rfu.config.categories.DailyStreakSettings
import cloud.glitchdev.rfu.events.managers.DailyStreakEvents
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeRegistry
import cloud.glitchdev.rfu.feature.streak.challenge.challenges.general.DailyAnglerChallenge
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.TextUtils
import net.minecraft.network.chat.Component
import java.time.LocalDate
import java.time.ZoneOffset

object DailyStreakManager {
    private val file = JsonFile("data", "daily_streak.json", DailyStreakData::class.java, { DailyStreakData() })
    val data: DailyStreakData get() = file.data

    private var listenersActivated = false

    fun getTodayDateString(): String = LocalDate.now(ZoneOffset.UTC).toString()
    fun getYesterdayDateString(): String = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString()

    fun saveData() {
        file.save()
        DailyStreakEvents.runTasks(data)
    }

    fun checkDailyReset() {
        if (!DailyStreakSettings.dailyStreakEnabled) return

        val today = getTodayDateString()
        val expectedCount = (1 + ChallengeRegistry.getPoolChallenges().size.coerceAtMost(2)).coerceAtLeast(1)
        if (data.currentDate == today && data.todayChallenges.size == expectedCount) {
            activateTodayListeners()
            return
        }

        performDailyReset(today)
    }

    private fun performDailyReset(today: String) {
        val yesterday = getYesterdayDateString()
        if (data.lastCompletedDate.isNotEmpty() && data.lastCompletedDate != yesterday && data.lastCompletedDate != today) {
            data.currentStreak = 0
        }

        data.currentDate = today
        data.hasRerolledToday = false

        val mandatory = ChallengeRegistry.getMandatoryChallenge() ?: DailyAnglerChallenge
        val username = try { User.getUsername() } catch (e: Exception) { "" }
        val seed = "$today:$username".hashCode().toLong()
        val poolChallenges = ChallengeRegistry.getSeededPoolChallenges(seed, 2)

        val challenge1 = DailyChallenge(mandatory.id)
        val challenge2 = poolChallenges.getOrNull(0)?.let { DailyChallenge(it.id) }
        val challenge3 = poolChallenges.getOrNull(1)?.let { DailyChallenge(it.id) }

        data.todayChallenges = listOfNotNull(challenge1, challenge2, challenge3)

        listenersActivated = false
        saveData()
        activateTodayListeners()
    }

    fun activateTodayListeners() {
        if (listenersActivated) return
        unregisterAllListeners()
        data.todayChallenges.forEach { challengeData ->
            if (!challengeData.isCompleted) {
                ChallengeRegistry.getChallenge(challengeData.id)?.setupListeners()
            }
        }
        listenersActivated = true
    }

    fun unregisterAllListeners() {
        listenersActivated = false
        ChallengeRegistry.getPoolChallenges().forEach { it.unregisterListeners() }
        ChallengeRegistry.getMandatoryChallenge()?.unregisterListeners()
    }

    fun addProgressForChallenge(challengeId: String, amount: Int = 1) {
        if (!DailyStreakSettings.dailyStreakEnabled) return
        checkDailyReset()

        var updated = false
        val streak = data.currentStreak

        data.todayChallenges.forEach { challenge ->
            if (challenge.id == challengeId && !challenge.isCompleted) {
                val target = challenge.getTargetProgress(streak)
                challenge.currentProgress = (challenge.currentProgress + amount).coerceAtMost(target)
                updated = true

                if (challenge.currentProgress >= target) {
                    challenge.isCompleted = true
                    data.totalChallengesCompleted++

                    ChallengeRegistry.getChallenge(challenge.id)?.unregisterListeners()

                    if (DailyStreakSettings.completionSound) {
                        Sounds.playSound("rfu:achievement", 1f, DailyStreakSettings.completionVolume)
                    }
                    val title = challenge.getTitle(streak)
                    Chat.sendMessage(TextUtils.rfuLiteral("Daily Challenge Completed: §e${title}§a!"))
                }
            }
        }

        if (updated) {
            checkAllChallengesCompleted()
            saveData()
        }
    }

    private fun checkAllChallengesCompleted() {
        if (data.todayChallenges.all { it.isCompleted } && data.lastCompletedDate != data.currentDate) {
            data.lastCompletedDate = data.currentDate
            data.currentStreak++
            data.totalDaysCompleted++
            if (data.currentStreak > data.highestStreak) {
                data.highestStreak = data.currentStreak
            }

            if (DailyStreakSettings.completionSound) {
                Sounds.playSound("rfu:achievement", 1.2f, DailyStreakSettings.completionVolume)
            }
            Chat.sendMessage(Component.literal("§b§l[§f§lRFU§b§l] §f\uE11F§6 Daily Streak Maintained! §eCurrent Streak: ${data.currentStreak} Days! §f\uE11F"))
        }
    }

    fun canReroll(): Boolean = !data.hasRerolledToday

    fun rerollChallenge(challengeId: String): Boolean {
        if (!DailyStreakSettings.dailyStreakEnabled) return false
        checkDailyReset()

        if (data.hasRerolledToday) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cYou have already used your daily challenge reroll today!"))
            return false
        }

        val targetIndex = data.todayChallenges.indexOfFirst { it.id == challengeId }
        if (targetIndex == -1) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cChallenge not found!"))
            return false
        }

        val targetChallenge = data.todayChallenges[targetIndex]
        if (targetChallenge.isCompleted) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cYou cannot reroll a completed challenge!"))
            return false
        }

        val targetBase = ChallengeRegistry.getChallenge(targetChallenge.id)
        if (targetBase?.isMandatoryBase == true) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cYou cannot reroll the mandatory daily challenge!"))
            return false
        }

        val activeBaseIds = data.todayChallenges.map { it.id }.toSet()
        val availablePool = ChallengeRegistry.getPoolChallenges().filter { it.id !in activeBaseIds }

        if (availablePool.isEmpty()) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cNo other daily challenges available to reroll into!"))
            return false
        }

        val newBase = ChallengeRegistry.getWeightedRandomChallenge(availablePool) ?: return false
        val newChallengeData = DailyChallenge(newBase.id)

        targetBase?.unregisterListeners()

        val updatedList = data.todayChallenges.toMutableList()
        updatedList[targetIndex] = newChallengeData
        data.todayChallenges = updatedList
        data.hasRerolledToday = true

        listenersActivated = false
        saveData()
        activateTodayListeners()

        val streak = data.currentStreak
        val title = newChallengeData.getTitle(streak)

        if (DailyStreakSettings.completionSound) {
            Sounds.playSound("rfu:achievement", 1f, DailyStreakSettings.completionVolume)
        }
        Chat.sendMessage(TextUtils.rfuLiteral("§aRerolled challenge to: §e${title}§a!"))
        return true
    }
}
