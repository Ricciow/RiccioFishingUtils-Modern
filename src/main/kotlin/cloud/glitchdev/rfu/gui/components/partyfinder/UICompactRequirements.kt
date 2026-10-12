package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.elementa.UIVanillaTooltip
import cloud.glitchdev.rfu.model.data.DataOption
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.MinConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.basicWidthConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.dsl.width
import gg.essential.elementa.renderer.ElementaExtractor
import net.minecraft.network.chat.Component
import kotlin.math.min

class UICompactRequirements(private val options: List<DataOption>) : UIContainer() {
    private val padding = UIScheme.pfCardSmallPadding
    private val badgePadding = (UIScheme.pfConditionCardPadding + UIScheme.pfConditionCardWidth) * 2
    private var visibleCount = -1
    private var lastWidth = -1f
    private var lastLayout: CompactRequirementsLayout? = null
    private var lastCardWidths = emptyList<Float>()
    private val cards = options.map { option ->
        UIConditionCard(option).constrain {
            y = 0.pixels
        }.childOf(this).also { it.hide(true) }
    }
    private val moreText = UIText("+${options.size} more")
    private val more = UIVanillaTooltip(tooltip = {
        listOf(Component.literal("${TextColor.DARK_GRAY}Other requirements:")) +
            cards.drop(visibleCount.coerceAtLeast(0)).map { card ->
                Component.literal(card.option.label).withStyle { style ->
                    style.withColor(card.textUI.getColor().rgb and 0xFFFFFF)
                }
            }
    }).constrain {
        y = 0.pixels
        width = basicWidthConstraint {
            val hiddenCount = options.size - visibleCount.coerceAtLeast(0)
            min(summaryWidth(hiddenCount), this@UICompactRequirements.getWidth().coerceAtLeast(0f))
        }
        height = (4.5f + badgePadding).pixels
    } childOf this

    init {
        val border = UIRoundedRectangle(5f).constrain {
            width = 100.percent
            height = 100.percent
            color = UIScheme.pfConditionCardUnknownBorder.toConstraint()
        } childOf more
        val background = UIRoundedRectangle(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = max(0.pixels, 100.percent - (UIScheme.pfConditionCardWidth * 2).pixels)
            height = max(0.pixels, 100.percent - (UIScheme.pfConditionCardWidth * 2).pixels)
            color = UIScheme.pfConditionCardUnknown.toConstraint()
        } childOf border
        moreText.constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = MinConstraint(
                ScaledTextConstraint(0.5f), max(0.pixels, 100.percent - (UIScheme.pfConditionCardPadding * 2).pixels)
            )
            height = TextAspectConstraint()
            color = UIScheme.pfConditionCardUnknownBorder.toConstraint()
        } childOf background
        more.onMouseClick { it.stopPropagation() }
        more.hide(true)
    }

    private fun summaryWidth(hiddenCount: Int): Float =
        "+$hiddenCount more".width(0.5f, moreText.getFontProvider()) + badgePadding

    override fun extractComponent(extractor: ElementaExtractor) {
        val availableWidth = getWidth().coerceAtLeast(0f)
        val widths = cards.map { it.getWidth() }
        val layout = compactRequirementsLayout(
            widths, availableWidth, padding,
            (1..options.size).map { summaryWidth(it) },
        )
        val count = layout.visibleCount

        if (layout != lastLayout || widths != lastCardWidths || availableWidth != lastWidth) {
            visibleCount = count
            lastWidth = availableWidth
            lastLayout = layout
            lastCardWidths = widths
            var offset = 0f
            cards.forEachIndexed { index, card ->
                if (index < count) {
                    card.setX(offset.pixels)
                    card.unhide(true)
                    offset += widths[index] + layout.gap
                } else card.hide(true)
            }
            if (count < options.size) {
                moreText.setText("+${options.size - count} more")
                more.setX(offset.pixels)
                more.unhide(true)
            } else more.hide(true)
        }
        super.extractComponent(extractor)
    }

    private data class CompactRequirementsLayout(val visibleCount: Int, val gap: Float)

    private fun compactRequirementsLayout(
        widths: List<Float>,
        availableWidth: Float,
        maxGap: Float,
        overflowWidths: List<Float>,
    ): CompactRequirementsLayout {
        val rowWidth = availableWidth.coerceAtLeast(0f)
        for (count in widths.size downTo 0) {
            val hiddenCount = widths.size - count
            val overflowWidth = if (hiddenCount > 0) overflowWidths[hiddenCount - 1] else 0f
            val overflowInRow = hiddenCount > 0
            val contentWidth = widths.take(count).sum() + if (overflowInRow) min(overflowWidth, rowWidth) else 0f
            if (contentWidth > rowWidth) continue

            val gaps = (count + if (overflowInRow) 1 else 0) - 1
            val gap = if (gaps > 0) min(maxGap, (rowWidth - contentWidth) / gaps) else 0f
            return CompactRequirementsLayout(count, gap)
        }
        return CompactRequirementsLayout(0, 0f)
    }
}
