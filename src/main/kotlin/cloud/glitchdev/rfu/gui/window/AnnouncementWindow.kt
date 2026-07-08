package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.utils.dsl.toFormattedDate
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.markdown.MarkdownComponent

class AnnouncementWindow(val announcement: Announcement) : BaseWindow() {

    init {
        create()
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

        UIText(announcement.title).constrain {
            x = UIScheme.pfSpacing.pixels()
            y = CenterConstraint()
            width = ScaledTextConstraint(1.5f)
            height = TextAspectConstraint()
            color = UIScheme.pfTitleText.toConstraint()
        } childOf header

        UIText(announcement.issuedAt.toFormattedDate()).constrain {
            x = UIScheme.pfSpacing.pixels(true)
            y = CenterConstraint()
            width = ScaledTextConstraint(1f)
            height = TextAspectConstraint()
            color = UIScheme.secondaryTextColor.toConstraint()
        } childOf header

        UIBlock().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent() - UIScheme.pfSpacing.pixels()
            height = 1.pixels()
            color = UIScheme.pfWindowSeparator.toConstraint()
        } childOf useableArea

        val contentWrapper = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 100.percent() - (2 * UIScheme.pfSpacing).pixels()
            height = FillConstraint() - 22.pixels
        } childOf useableArea effect ScissorEffect()

        val scrollArea = ScrollComponent().constrain {
            x = 0.pixels()
            y = UIScheme.pfSmallSpacing.pixels()
            width = FillConstraint() - UIScheme.pfSmallSpacing.pixels()
            height = 100.percent() - UIScheme.pfSmallSpacing.pixels()
        } childOf contentWrapper

        val scrollBar = UIRoundedRectangle(5f).constrain {
            x = 0.pixels(true)
            width = 3.pixels()
            color = UIScheme.pfScrollBar.toConstraint()
        } childOf contentWrapper

        scrollArea.setScrollBarComponent(scrollBar, hideWhenUseless = true, isHorizontal = false)

        MarkdownComponent(announcement.content).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 100.percent()
        } childOf scrollArea
    }
}