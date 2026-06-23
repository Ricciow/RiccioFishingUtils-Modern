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
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import cloud.glitchdev.rfu.gui.components.colors

object AchievementWindow : BaseWindow() {
    private var selectedCategory: AchievementCategory = AchievementCategory.GENERAL
    private lateinit var scrollArea: ScrollComponent
    private val achievementComponents = mutableListOf<Achievement>()
    private val categoryButtons = mutableListOf<Pair<AchievementCategory, UIButton>>()
    private var needsRefresh = false

    init {
        create()

        registerTickEvent {
            //~ if >=26.2 'screen' -> 'gui.screen()' {
            if (needsRefresh && RiccioFishingUtils.mc.gui.screen() == this) {
            //~}
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
        val radius = 5f
        
        val background = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 80.percent()
            height = 80.percent()
            color = UIScheme.pfWindowBackground.toConstraint()
        } childOf window

        val useableArea = UIContainer().constrain {
            x = CenterConstraint()
            y = (radius / 2).pixels()
            width = 100.percent()
            height = 100.percent() - radius.pixels()
        } childOf background

        val header = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = 30.pixels()
        } childOf useableArea effect ScissorEffect()

        UIText("RFU Achievements").constrain {
            x = UIScheme.pfSpacing.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(1.5f)
            height = TextAspectConstraint()
            color = UIScheme.pfTitleText.toConstraint()
        } childOf header

        val categoryContainer = UIContainer().constrain {
            x = UIScheme.pfSpacing.pixels(true)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 100.percent() - 5.pixels()
        } childOf header

        AchievementCategory.entries.forEach { category ->
            val iconPath = when (category) {
                AchievementCategory.GENERAL -> "/assets/rfu/ui/water.png"
                AchievementCategory.ISLE -> "/assets/rfu/ui/lava.png"
                AchievementCategory.HOT_SPOT -> "/assets/rfu/ui/location.png"
                AchievementCategory.INK -> "/assets/rfu/ui/ink_sac.png"
                AchievementCategory.SPECIAL -> "/assets/rfu/ui/looting_5.png"
            }
            val image = UIImage.ofResource(iconPath)
            val btn = UIButton.withImage(image, 5f) {
                selectedCategory = category
                refreshAchievements()
            }
            btn.constrain {
                x = SiblingConstraint(UIScheme.pfSmallSpacing)
                y = CenterConstraint()
                width = AspectConstraint(1f)
                height = 100.percent()
            } childOf categoryContainer
            categoryButtons.add(Pair(category, btn))
        }

        UIBlock().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent() - UIScheme.pfSpacing.pixels()
            height = 1.pixels()
            color = UIScheme.pfWindowSeparator.toConstraint()
        } childOf useableArea

        val scrollContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent() - (2 * UIScheme.pfSpacing).pixels()
            height = FillConstraint()
        } childOf useableArea effect ScissorEffect()

        val scrollbar = UIRoundedRectangle(5f).constrain {
            x = 0.pixels(true)
            width = 3.pixels()
            color = UIScheme.pfTitleText.toConstraint()
        } childOf scrollContainer

        scrollArea = ScrollComponent().constrain {
            x = 0.pixels()
            y = UIScheme.pfSmallSpacing.pixels()
            width = 100.percent() - 7.pixels()
            height = 100.percent() - UIScheme.pfSmallSpacing.pixels()
        } childOf scrollContainer

        scrollArea.setScrollBarComponent(scrollbar, hideWhenUseless = true, isHorizontal = false)

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
        categoryButtons.forEach { (cat, btn) ->
            if (selectedCategory == cat) {
                btn.colors {
                    textColor = UIScheme.pfFilterButtonSelected.toConstraint()
                    hoverTextColor = UIScheme.pfFilterButtonSelected.toConstraint()
                    primaryColor = UIScheme.pfFilterButtonSelectedBg.toConstraint()
                    hoverColor = UIScheme.pfInputBgHovered.toConstraint()
                }
            } else {
                btn.colors {
                    textColor = UIScheme.primaryTextColor.toConstraint()
                    hoverTextColor = UIScheme.primaryTextColor.toConstraint()
                    primaryColor = UIScheme.pfInputBg.toConstraint()
                    hoverColor = UIScheme.pfInputBgHovered.toConstraint()
                }
            }
        }

        val existingCardsMap = achievementComponents.associateBy { it.achievement.id }
        
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
                val existingCard = existingCardsMap[achievement.id]
                val wasHovered = existingCard?.isHovered() ?: false
                
                val component = Achievement(achievement, wasHovered).constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint(5f)
                    width = 100.percent()
                } childOf scrollArea
                achievementComponents.add(component)
            }
    }
}
