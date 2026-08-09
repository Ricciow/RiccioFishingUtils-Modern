package cloud.glitchdev.rfu.data.streak

import cloud.glitchdev.rfu.config.categories.DailyStreakSettings
import cloud.glitchdev.rfu.events.managers.DailyStreakEvents
import cloud.glitchdev.rfu.feature.streak.challenge.BaseChallenge
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeRegistry
import cloud.glitchdev.rfu.feature.streak.challenge.challenges.DailyAnglerChallenge
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.TextUtils
import net.minecraft.network.chat.Component
import java.time.LocalDate
import java.time.ZoneOffset

object DailyStreakManager {
    private val file = JsonFile("data", "daily_streak.json", DailyStreakData::class.java, { DailyStreakData() })
    val data: DailyStreakData get() = file.data

    fun getTodayDateString(): String = LocalDate.now(ZoneOffset.UTC).toString()
    fun getYesterdayDateString(): String = LocalDate.now(ZoneOffset.UTC).minusDays(1).toString()

    private fun createChallengeData(base: BaseChallenge, today: String): DailyChallenge {
        val target = base.getTargetProgress(data.currentStreak)
        return DailyChallenge(
            id = "${base.id}_$today",
            title = base.title,
            description = base.description,
            currentProgress = 0,
            targetProgress = target
        )
    }

    fun checkDailyReset() {
        if (!DailyStreakSettings.dailyStreakEnabled) return

        val today = getTodayDateString()
        val expectedCount = (1 + ChallengeRegistry.getPoolChallenges().size.coerceAtMost(2)).coerceAtLeast(1)
        if (data.currentDate == today && data.todayChallenges.size == expectedCount) {
            activateTodayListeners()
            return
        }

        val yesterday = getYesterdayDateString()
        if (data.lastCompletedDate != yesterday && data.lastCompletedDate != today && data.lastCompletedDate.isNotEmpty()) {
            data.currentStreak = 0
        }

        data.currentDate = today
        data.hasRerolledToday = false

        val mandatory = ChallengeRegistry.getMandatoryChallenge() ?: DailyAnglerChallenge
        val challenge1 = createChallengeData(mandatory, today)

        val seed = today.hashCode().toLong()
        val poolChallenges = ChallengeRegistry.getSeededPoolChallenges(seed, 2)

        val challenge2 = poolChallenges.getOrNull(0)?.let { createChallengeData(it, today) }
        val challenge3 = poolChallenges.getOrNull(1)?.let { createChallengeData(it, today) }

        data.todayChallenges = listOfNotNull(challenge1, challenge2, challenge3)
        file.save()

        activateTodayListeners()
        DailyStreakEvents.runTasks(data)
    }

    fun activateTodayListeners() {
        unregisterAllListeners()
        data.todayChallenges.forEach { challengeData ->
            if (!challengeData.isCompleted) {
                val baseId = challengeData.id.substringBeforeLast("_")
                ChallengeRegistry.getChallenge(baseId)?.setupListeners()
            }
        }
    }

    fun unregisterAllListeners() {
        ChallengeRegistry.getPoolChallenges().forEach { it.unregisterListeners() }
        ChallengeRegistry.getMandatoryChallenge()?.unregisterListeners()
    }

    fun addProgressForChallenge(challengeId: String, amount: Int = 1) {
        if (!DailyStreakSettings.dailyStreakEnabled) return
        checkDailyReset()

        var updated = false

        data.todayChallenges.forEach { challenge ->
            val baseId = challenge.id.substringBeforeLast("_")
            if (baseId == challengeId && !challenge.isCompleted) {
                challenge.currentProgress = (challenge.currentProgress + amount).coerceAtMost(challenge.targetProgress)
                updated = true

                if (challenge.currentProgress >= challenge.targetProgress && !challenge.isCompleted) {
                    challenge.isCompleted = true
                    data.totalChallengesCompleted++

                    ChallengeRegistry.getChallenge(baseId)?.unregisterListeners()

                    if (DailyStreakSettings.completionSound) {
                        Sounds.playSound("rfu:achievement", 1f, DailyStreakSettings.completionVolume)
                    }
                    Chat.sendMessage(TextUtils.rfuLiteral("Daily Challenge Completed: §e${challenge.title}§a!"))
                }
            }
        }

        if (updated) {
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

            file.save()
            DailyStreakEvents.runTasks(data)
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

        val targetIndex = data.todayChallenges.indexOfFirst {
            val baseId = it.id.substringBeforeLast("_")
            baseId == challengeId || it.id == challengeId
        }

        if (targetIndex == -1) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cChallenge not found!"))
            return false
        }

        val targetChallenge = data.todayChallenges[targetIndex]
        if (targetChallenge.isCompleted) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cYou cannot reroll a completed challenge!"))
            return false
        }

        val targetBaseId = targetChallenge.id.substringBeforeLast("_")
        val targetBase = ChallengeRegistry.getChallenge(targetBaseId)
        if (targetBase?.isMandatoryBase == true) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cYou cannot reroll the mandatory daily challenge!"))
            return false
        }

        val activeBaseIds = data.todayChallenges.map { it.id.substringBeforeLast("_") }.toSet()
        val availablePool = ChallengeRegistry.getPoolChallenges().filter { it.id !in activeBaseIds }

        if (availablePool.isEmpty()) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cNo other daily challenges available to reroll into!"))
            return false
        }

        val newBase = availablePool.random()
        val today = data.currentDate
        val newChallengeData = createChallengeData(newBase, today)

        targetBase?.unregisterListeners()

        val updatedList = data.todayChallenges.toMutableList()
        updatedList[targetIndex] = newChallengeData
        data.todayChallenges = updatedList
        data.hasRerolledToday = true

        file.save()
        activateTodayListeners()
        DailyStreakEvents.runTasks(data)

        if (DailyStreakSettings.completionSound) {
            Sounds.playSound("rfu:achievement", 1f, DailyStreakSettings.completionVolume)
        }
        Chat.sendMessage(TextUtils.rfuLiteral("§aRerolled challenge to: §e${newChallengeData.title}§a!"))
        return true
    }
}
