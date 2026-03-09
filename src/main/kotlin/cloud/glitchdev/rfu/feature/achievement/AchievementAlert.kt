package cloud.glitchdev.rfu.feature.achievement

import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.constants.text.TextColor
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

@RFUFeature
object AchievementAlert : Feature {
    override fun onInitialize() {
        registerAchievementUnlockedEvent { achievement ->
            sendCompletedMessage(achievement)
        }

        registerAchievementStageUnlockedEvent { achievement ->
            sendStageCompletedMessage(achievement)
        }
    }

    private fun sendStageCompletedMessage(achievement: IStageAchievement) {
        Sounds.playSound("rfu:achievement")

        val pair = achievement.difficulty.makePair()
        val completedStage = achievement.currentStage - 1
        val stageName = achievement.getStageName(completedStage) ?: achievement.name
        val stageDesc = achievement.getStageDescription(completedStage) ?: achievement.description

        val achievementText = Component.literal("${pair.second}[$stageName]")
            .withStyle(
                Style.EMPTY
                    .withHoverEvent(HoverEvent.ShowText(Component.literal(stageDesc)))
            )

        Chat.sendMessage(
            TextUtils.rfuLiteral("${pair.first}! You just completed stage $completedStage of ")
                .append(achievementText)
        )
    }

    private fun sendCompletedMessage(achievement: IAchievement) {
        Sounds.playSound("rfu:achievement")

        val pair = achievement.difficulty.makePair()

        val achievementText = Component.literal("${pair.second}[${achievement.name}]")
            .withStyle(
                Style.EMPTY
                    .withHoverEvent(HoverEvent.ShowText(Component.literal(achievement.description)))
            )

        Chat.sendMessage(
            TextUtils.rfuLiteral("${pair.first}! You just completed ")
                .append(achievementText)
        )
    }

    private fun AchievementDifficulty.makePair() : Pair<String, TextColor> {
        return when(this) {
            AchievementDifficulty.EASY -> "Nice" to TextColor.LIGHT_GREEN
            AchievementDifficulty.MEDIUM -> "Solid" to TextColor.LIGHT_GREEN
            AchievementDifficulty.HARD -> "Incredible" to TextColor.LIGHT_GREEN
            AchievementDifficulty.VERY_HARD -> "Unreal" to TextColor.PURPLE
            AchievementDifficulty.IMPOSSIBLE -> "Legendary" to TextColor.PURPLE
        }
    }
}