package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.utils.dsl.toFormattedDate
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.markdown.MarkdownComponent

class AnnouncementWindow(val announcement: Announcement) : BaseWindow() {
    val primaryColor = UIScheme.primaryColorOpaque.toConstraint()
    val radius = 5f
    val windowSize = 0.8f

    init {
        create()
    }

    fun create() {
        val background = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = max(RelativeWindowConstraint(windowSize), 320.pixels())
            height = RelativeWindowConstraint(windowSize)
            color = primaryColor
        } childOf window

        val innerContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = max(90.percent(), 310.pixels())
            height = 90.percent()
        } childOf background

        val header = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf innerContainer

        UIText(announcement.title).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = TextAspectConstraint()
            height = ScaledTextConstraint(1.5f)
        } childOf header

        UIText(announcement.issuedAt.toFormattedDate()).constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = TextAspectConstraint()
            height = ScaledTextConstraint(1f)
        } childOf header

        MarkdownComponent(announcement.content).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(2f)
            width = 100.percent()
            height = FillConstraint()
        } childOf innerContainer
    }
}