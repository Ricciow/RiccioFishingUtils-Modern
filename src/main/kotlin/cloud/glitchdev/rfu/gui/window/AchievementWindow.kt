package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.RiccioFishingUtils
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementProvider
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.achievement.interfaces.IStageAchievement
import cloud.glitchdev.rfu.events.managers.AchievementStageUnlockedEvents.registerAchievementStageUnlockedEvent
import cloud.glitchdev.rfu.events.managers.AchievementUnlockedEvents.registerAchievementUnlockedEvent
import cloud.glitchdev.rfu.events.managers.AchievementUpdatedEvents.registerAchievementUpdatedEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.achievement.Achievement
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint

object AchievementWindow : BaseWindow() {
    private var selectedCategory: AchievementCategory = AchievementCategory.GENERAL
    private lateinit var scrollArea: ScrollComponent
    private val achievementComponents = mutableListOf<Achievement>()
    private var needsRefresh = false

    init {
        create()

        registerTickEvent {
            if (needsRefresh && RiccioFishingUtils.mc.screen == this) {
                refreshAchievements()
                needsRefresh = false
            }
        }

        registerAchievementUnlockedEvent { _ ->
            needsRefresh = true
        }

        registerAchievementStageUnlockedEvent { _ ->
            needsRefresh = true
        }

        registerAchievementUpdatedEvent { _ ->
            needsRefresh = true
        }
    }

    override fun onOpenWindow() {
        needsRefresh = false
        refreshAchievements()
    }

    fun create() {
        val background = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = RelativeWindowConstraint(0.8f)
            height = RelativeWindowConstraint(0.8f)
            color = UIScheme.primaryColorOpaque.toConstraint()
        } childOf window

        val header = UIContainer().constrain {
            x = CenterConstraint()
            y = 5.pixels()
            width = 96.percent()
            height = 20.pixels()
        } childOf background

        val categoryContainer = ScrollComponent(verticalScrollEnabled = false, horizontalScrollEnabled = true).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent()
            height = 100.percent()
        } childOf header

        AchievementCategory.entries.forEach { category ->
            UIButton(category.name.lowercase().replaceFirstChar { it.uppercase() }, 5f) {
                selectedCategory = category
                refreshAchievements()
            }.constrain {
                x = SiblingConstraint(5f)
                y = CenterConstraint()
                width = 80.pixels()
                height = 100.percent()
            } childOf categoryContainer
        }

        val scrollContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 96.percent()
            height = FillConstraint() - 15.pixels()
        } childOf background

        val scrollbar = UIRoundedRectangle(5f).constrain {
            x = 0.pixels(true)
            y = CenterConstraint()
            width = 5.pixels()
            height = 100.percent()
            color = UIScheme.secondaryColorOpaque.toConstraint()
        } childOf scrollContainer

        scrollArea = ScrollComponent().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent() - 7.pixels()
            height = 100.percent()
        } childOf scrollContainer

        scrollArea.setScrollBarComponent(scrollbar, hideWhenUseless = false, isHorizontal = false)

        refreshAchievements()
    }

    private fun IAchievement.getDisplayDifficulty(): AchievementDifficulty {
        return if (this is IStageAchievement && !this.isCompleted) {
            this.getStageDifficulty(this.currentStage) ?: this.difficulty
        } else {
            this.difficulty
        }
    }

    private fun refreshAchievements() {
        achievementComponents.forEach {
            scrollArea.removeChild(it)
        }
        achievementComponents.clear()

        AchievementProvider.getVisibleAchievements()
            .filter { it.category == selectedCategory }
            .sortedWith(compareBy(
                {
                    when {
                        it.isCompleted -> 2
                        it.type == AchievementType.SECRET -> 1
                        else -> 0
                    }
                },
                { it.getDisplayDifficulty() }
            ))
            .forEach { achievement ->
                val component = Achievement(achievement).constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint(5f)
                    width = 100.percent()
                    height = ChildBasedSizeConstraint()
                } childOf scrollArea
                achievementComponents.add(component)
            }
    }
}
