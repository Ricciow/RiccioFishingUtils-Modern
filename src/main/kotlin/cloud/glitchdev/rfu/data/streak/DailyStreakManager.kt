package cloud.glitchdev.rfu.data.streak

import cloud.glitchdev.rfu.config.categories.DailyStreakSettings
import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.DailyStreakEvents
import cloud.glitchdev.rfu.feature.streak.challenge.ChallengeRegistry
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.User
import cloud.glitchdev.rfu.utils.JsonFile
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

object DailyStreakManager {
    private val file = JsonFile("data", "daily_streak.json", DailyStreakData::class.java, { DailyStreakData() })
    val data: DailyStreakData get() = file.data

    private var dayScope: EventScope? = null
    private val challengeScopes = mutableMapOf<String, EventScope>()

    fun getTodayDateString(): String = LocalDate.now(ZoneOffset.UTC).toString()

    fun getTimeUntilReset(): Duration {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        return Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay())
    }

    fun saveData() {
        file.save()
        DailyStreakEvents.runTasks(data)
    }

    fun checkDailyReset() {
        if (!DailyStreakSettings.dailyStreakEnabled || !World.isOnHypixel || !World.isInSkyblock) return

        val today = getTodayDateString()
        val expectedCount = ChallengeRegistry.getPoolChallenges().size.coerceAtMost(3).coerceAtLeast(1)
        if (data.currentDate == today && data.todayChallenges.size == expectedCount) {
            if (dayScope == null) updateChallengeListeners()
            return
        }

        performDailyReset(today)
    }

    private fun performDailyReset(today: String) {
        if (data.currentDate.isNotEmpty() && data.currentDate != today && data.lastCompletedDate != data.currentDate) {
            data.currentStreak = 0
        }

        data.currentDate = today
        data.hasRerolledToday = false

        val username = try { User.getUsername() } catch (_: Exception) { "" }
        val seed = "$today:$username".hashCode().toLong()
        val poolChallenges = ChallengeRegistry.getSeededPoolChallenges(seed, 3)

        unregisterAllListeners()
        data.todayChallenges = poolChallenges.map { DailyChallenge(it.id) }

        saveData()
        if (dayScope == null) updateChallengeListeners()
    }

    private fun updateChallengeListeners() {
        val parent = dayScope ?: EventScope().also { dayScope = it }
        val activeChallenges = data.todayChallenges.asSequence()
            .filterNot { it.isCompleted }
            .mapNotNull { it.baseDef }
            .associateBy { it.id }

        try {
            (challengeScopes.keys - activeChallenges.keys).forEach(::closeChallengeScope)
            for ((id, challenge) in activeChallenges) {
                if (id in challengeScopes) continue
                val scope = parent.child()
                challengeScopes[id] = scope
                challenge.setupListeners(scope)
            }
        } catch (failure: Throwable) {
            try {
                unregisterAllListeners()
            } catch (cleanupFailure: Throwable) {
                if (cleanupFailure !== failure) failure.addSuppressed(cleanupFailure)
            }
            throw failure
        }
    }

    private fun closeChallengeScope(id: String) {
        challengeScopes.remove(id)?.close()
    }

    fun unregisterAllListeners() {
        val scope = dayScope
        dayScope = null
        challengeScopes.clear()
        scope?.close()
    }

    fun addProgressForChallenge(challengeId: String, amount: Int = 1) {
        if (!DailyStreakSettings.dailyStreakEnabled || !World.isOnHypixel || !World.isInSkyblock) return
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
                    if (data.todayChallenges.none { it.id == challengeId && !it.isCompleted }) {
                        closeChallengeScope(challengeId)
                    }

                    val title = challenge.getTitle(streak)
                    Chat.sendMessage(TextUtils.rfuLiteral("${TextColor.LIGHT_GREEN}Daily Challenge Completed: ${TextColor.YELLOW}${title}${TextColor.LIGHT_GREEN}!"))
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
                Sounds.playSound("rfu:daily_challenge", 1f, DailyStreakSettings.completionVolume)
            }
            Chat.sendMessage(TextUtils.rfuLiteral("").append(Emoji.icon("fire").component())
                .append("§6 Daily Streak Maintained! §eCurrent Streak: ${data.currentStreak} Days! §f")
                .append(Emoji.icon("fire").component()))
        }
    }

    fun canReroll(): Boolean = !data.hasRerolledToday

    fun rerollChallenge(challengeId: String): Boolean {
        if (!DailyStreakSettings.dailyStreakEnabled || !World.isOnHypixel || !World.isInSkyblock) return false
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

        val activeBaseIds = data.todayChallenges.map { it.id }.toSet()
        val availablePool = ChallengeRegistry.getPoolChallenges().filter { it.id !in activeBaseIds }

        if (availablePool.isEmpty()) {
            Chat.sendMessage(TextUtils.rfuLiteral("§cNo other daily challenges available to reroll into!"))
            return false
        }

        val newBase = ChallengeRegistry.getWeightedRandomChallenge(availablePool) ?: return false
        val newChallengeData = DailyChallenge(newBase.id)

        val updatedList = data.todayChallenges.toMutableList()
        updatedList[targetIndex] = newChallengeData
        data.todayChallenges = updatedList
        data.hasRerolledToday = true

        updateChallengeListeners()
        saveData()

        val streak = data.currentStreak
        val title = newChallengeData.getTitle(streak)

        Chat.sendMessage(TextUtils.rfuLiteral("§aRerolled challenge to: §e${title}§a!"))
        return true
    }

    fun setChallenge(challengeId: String, index: Int = 0): Boolean {
        checkDailyReset()
        val targetBase = ChallengeRegistry.getChallenge(challengeId) ?: return false

        val updatedList = data.todayChallenges.toMutableList()
        if (updatedList.getOrNull(index)?.id == targetBase.id) {
            closeChallengeScope(targetBase.id)
        }

        val newChallengeData = DailyChallenge(targetBase.id)
        if (index in updatedList.indices) {
            updatedList[index] = newChallengeData
        } else {
            updatedList.add(newChallengeData)
        }

        data.todayChallenges = updatedList
        updateChallengeListeners()
        saveData()
        return true
    }
}
