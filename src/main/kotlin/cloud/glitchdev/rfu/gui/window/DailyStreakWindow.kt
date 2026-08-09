package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.RiccioFishingUtils
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.data.streak.DailyStreakManager
import cloud.glitchdev.rfu.events.managers.DailyStreakEvents.registerStreakUpdatedEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.streak.UIDailyChallengeCard
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.times
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.effects.ScissorEffect
import java.awt.Color
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

object DailyStreakWindow : BaseWindow(false) {
    private val primaryColor = UIScheme.pfWindowBackground.toConstraint()
    private val headerHeight = 30.pixels
    private val spacing = UIScheme.pfSpacing
    private val smallSpacing = UIScheme.pfSmallSpacing

    private lateinit var scrollArea: ScrollComponent
    private lateinit var streakTextComponent: UIText
    private lateinit var highestTextComponent: UIText
    private lateinit var completedTextComponent: UIText
    private lateinit var resetTimerTextComponent: UIText

    private val challengeCards = mutableListOf<UIDailyChallengeCard>()
    private var needsRefresh = false

    init {
        create()

        registerTickEvent {
            //~ if >=26.2 'screen' -> 'gui.screen()' {
            if (RiccioFishingUtils.mc.gui.screen() == this) {
            //~}
                if (needsRefresh) {
                    refreshWindow()
                    needsRefresh = false
                } else {
                    updateResetTimer()
                }
            }
        }

        registerStreakUpdatedEvent {
            needsRefresh = true
        }
    }

    override fun onOpenWindow() {
        DailyStreakManager.checkDailyReset()
        refreshWindow()
    }

    private fun create() {
        val radius = 5f

        val background = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 80.percent()
            height = 80.percent()
            color = primaryColor
        } childOf window

        val useableArea = UIContainer().constrain {
            x = CenterConstraint()
            y = (radius / 2).pixels()
            width = 100.percent()
            height = 100.percent() - radius.pixels()
        } childOf background

        createHeader(useableArea)
        createChallengesHeader(useableArea)
        createChallengesArea(useableArea)
    }

    private fun createHeader(background: UIContainer) {
        val header = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = headerHeight
        } childOf background effect ScissorEffect()

        UIText("RFU Daily Streaks").constrain {
            x = spacing.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(1.5f)
            height = TextAspectConstraint()
            color = UIScheme.pfTitleText.toConstraint()
        } childOf header

        val statsArea = UIContainer().constrain {
            x = spacing.pixels(true)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 100.percent()
        } childOf header

        streakTextComponent = UIText("").constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(1.0f)
            height = TextAspectConstraint()
            color = Color(255, 170, 0).toConstraint()
        } childOf statsArea

        highestTextComponent = UIText("").constrain {
            x = SiblingConstraint(12f)
            y = CenterConstraint()
            width = ScaledTextConstraint(1.0f)
            height = TextAspectConstraint()
            color = Color(255, 215, 0).toConstraint()
        } childOf statsArea

        completedTextComponent = UIText("").constrain {
            x = SiblingConstraint(12f)
            y = CenterConstraint()
            width = ScaledTextConstraint(1.0f)
            height = TextAspectConstraint()
            color = UIScheme.achievementCompleteColor.toConstraint()
        } childOf statsArea

        UIBlock().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent() - spacing.pixels()
            height = 1.pixels()
            color = UIScheme.pfWindowSeparator.toConstraint()
        } childOf background
    }

    private fun createChallengesHeader(background: UIContainer) {
        val challengesHeader = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(smallSpacing)
            width = 100.percent() - (2 * spacing).pixels()
            height = 18.pixels()
        } childOf background

        UIText("Today's Challenges").constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(1.2f)
            height = TextAspectConstraint()
            color = UIScheme.primaryTextColor.toConstraint()
        } childOf challengesHeader

        resetTimerTextComponent = UIText("").constrain {
            x = 0.pixels(true)
            y = CenterConstraint()
            width = ScaledTextConstraint(0.9f)
            height = TextAspectConstraint()
            color = UIScheme.secondaryTextColor.toConstraint()
        } childOf challengesHeader
    }

    private fun createChallengesArea(background: UIContainer) {
        val scrollContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(smallSpacing)
            width = 100.percent() - (2 * spacing).pixels()
            height = FillConstraint() - smallSpacing.pixels * 2
        } childOf background effect ScissorEffect()

        val scrollbar = UIRoundedRectangle(5f).constrain {
            x = 0.pixels(true)
            width = 3.pixels()
            color = UIScheme.pfScrollBar.toConstraint()
        } childOf scrollContainer

        scrollArea = ScrollComponent().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent() - 7.pixels()
            height = 100.percent()
        } childOf scrollContainer

        scrollArea.setScrollBarComponent(scrollbar, hideWhenUseless = true, isHorizontal = false)
    }

    private fun updateResetTimer() {
        if (!::resetTimerTextComponent.isInitialized) return
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val durationUntilReset = Duration.between(now, midnight)
        val hours = durationUntilReset.toHours()
        val minutes = durationUntilReset.toMinutes() % 60
        resetTimerTextComponent.setText("Resets in: ${hours}h ${minutes}m (00:00 UTC)")
    }

    private fun refreshWindow() {
        val data = DailyStreakManager.data

        if (::streakTextComponent.isInitialized) {
            streakTextComponent.setText("${TextColor.WHITE}\uE11F${TextEffects.RESET} ${data.currentStreak} Days")
            highestTextComponent.setText("🏆 Best: ${data.highestStreak} Days")
            completedTextComponent.setText("✔ Total: ${data.totalChallengesCompleted}")
        }
        updateResetTimer()

        if (!::scrollArea.isInitialized) return

        val existingCardsMap = challengeCards.associateBy { it.challenge.id }
        scrollArea.clearChildren()
        challengeCards.clear()

        data.todayChallenges.forEachIndexed { index, challenge ->
            val existingCard = existingCardsMap[challenge.id]
            val wasHovered = existingCard?.isHovered() ?: false

            val card = UIDailyChallengeCard(
                challenge,
                index,
                wasHovered = wasHovered,
                onRerollClick = { c ->
                    DailyStreakManager.rerollChallenge(c.id)
                    refreshWindow()
                }
            ).constrain {
                x = CenterConstraint()
                y = SiblingConstraint(smallSpacing + 2f)
                width = 100.percent()
            } childOf scrollArea

            challengeCards.add(card)
        }
    }
}
