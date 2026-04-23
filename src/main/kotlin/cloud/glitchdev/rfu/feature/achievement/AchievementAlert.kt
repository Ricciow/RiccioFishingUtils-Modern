package cloud.glitchdev.rfu.feature.achievement

import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.events.managers.AchievementStageUnlockedEvents.registerAchievementStageUnlockedEvent
import cloud.glitchdev.rfu.events.managers.AchievementUnlockedEvents.registerAchievementUnlockedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.TextUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@RFUFeature
object AchievementAlert : Feature {
    private var lastSoundPlayTime: Instant = Instant.DISTANT_PAST

    override fun onInitialize() {
        registerAchievementUnlockedEvent { achievement ->
            sendCompletedMessage(achievement)
        }

        registerAchievementStageUnlockedEvent { achievement ->
            sendStageCompletedMessage(achievement)
        }
    }

    private fun playSound() {
        val now = Clock.System.now()
        if (now - lastSoundPlayTime >= 1.seconds) {
            if (OtherSettings.achievementSound) {
                Sounds.playSound("rfu:achievement", 1f, OtherSettings.achievementVolume)
            }
            lastSoundPlayTime = now
        }
    }

    private fun sendStageCompletedMessage(achievement: IStageAchievement) {
        if(achievement.currentStage > achievement.targetStage) return

        playSound()

        val completedStage = achievement.currentStage - 1
        val stageDifficulty = achievement.getStageDifficulty(completedStage) ?: achievement.difficulty
        val pair = stageDifficulty.makePair()

        val stageName = achievement.getStageName(completedStage) ?: achievement.name
        val stageDesc = achievement.getStageDescription(completedStage) ?: achievement.description

        val achievementText = Component.literal("${pair.second}[$stageName]")
            .withStyle(
                Style.EMPTY
                    .withHoverEvent(HoverEvent.ShowText(Component.literal(stageDesc)))
            )

        val message = if(achievement.getStageName(completedStage) == null) {
            "${TextColor.YELLOW}${TextEffects.BOLD}${pair.first}! ${TextColor.GOLD}You just completed stage $completedStage of "
        } else {
            "${TextColor.YELLOW}${TextEffects.BOLD}${pair.first}! ${TextColor.GOLD}You just completed: "
        }

        Chat.sendMessage(
            TextUtils.rfuLiteral(message)
                .append(achievementText)
        )
    }

    private fun sendCompletedMessage(achievement: IAchievement) {
        playSound()

        var name = achievement.name
        var desc = achievement.description
        var difficulty = achievement.difficulty

        if (achievement is IStageAchievement) {
            val lastStage = achievement.targetStage
            name = achievement.getStageName(lastStage) ?: name
            desc = achievement.getStageDescription(lastStage) ?: desc
            difficulty = achievement.getStageDifficulty(lastStage) ?: difficulty
        }

        val pair = difficulty.makePair()

        val achievementText = Component.literal("${pair.second}[$name]")
            .withStyle(
                Style.EMPTY
                    .withHoverEvent(HoverEvent.ShowText(Component.literal(desc)))
            )

        Chat.sendMessage(
            TextUtils.rfuLiteral("${TextColor.YELLOW}${TextEffects.BOLD}${pair.first}! ${TextColor.GOLD}You just completed: ")
                .append(achievementText)
        )
    }

    private fun AchievementDifficulty.makePair() : Pair<String, TextColor> {
        return when(this) {
            AchievementDifficulty.EASY -> listOf("Nice", "Easy", "Simple", "Clean", "Quick", "Smooth").random() to TextColor.LIGHT_GREEN
            AchievementDifficulty.MEDIUM -> listOf("Solid", "Decent", "Great", "Good", "Respectable", "Well Done", "Impressive", "Nice Job").random() to TextColor.LIGHT_GREEN
            AchievementDifficulty.HARD -> listOf("Incredible", "Amazing", "Awesome", "Superb", "Skilled", "Outstanding", "Fantastic", "Brilliant").random() to TextColor.LIGHT_GREEN
            AchievementDifficulty.VERY_HARD -> listOf("Unreal", "Insane", "Mind-blowing", "Heroic", "Cracked", "Epic", "Unstoppable", "Absolute").random() to TextColor.PURPLE
            AchievementDifficulty.IMPOSSIBLE -> listOf("Legendary", "Mythical", "Godly", "Beyond", "Ultimate", "Impossible", "Eternal", "Divine").random() to TextColor.PURPLE
        }
    }
}