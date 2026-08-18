package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ArmorEvents.registerArmorChangeEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.PlainTextContents

@Achievement
object MinMaxingAchievement : BaseAchievement() {
    override val id: String = "min_maxing"
    override val name: String = "MinMaxing"
    override val description: String = "Hold a 10 Starred Hellfire rod while wearing full 10 Starred Magma Lord."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override fun setupListeners() {
        activeListeners.add(registerArmorChangeEvent {
            checkCompletion()
        })
        activeListeners.add(registerTickEvent(interval = 40) {
            checkCompletion()
        })
    }

    private fun checkCompletion() {
        val player = mc.player ?: return
        val mainHand = player.mainHandItem.customName ?: return

        if (mainHand.isValidTarget("Hellfire Rod") && ArmorEvents.currentArmorSet.isWearingMagmaLord10StarredSet) {
            complete()
        }
    }

    private const val STAR_COLOR = 0xFF55FF
    private const val STAR_STRING = "✪✪✪✪✪"

    private fun Component.isValidTarget(expectedName: String): Boolean {
        return this.toUnformattedString().contains(expectedName) && this.is10Starred()
    }

    private fun Component.is10Starred(): Boolean {
        return this.siblings.any { part ->
            val contents = part.contents
            contents is PlainTextContents.LiteralContents &&
                    contents.text.trim() == STAR_STRING &&
                    part.style.color?.value == STAR_COLOR
        }
    }
}