package cloud.glitchdev.rfu.gui.components.elementa

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import java.util.Optional
import java.util.function.Supplier

object TooltipRenderer {
    private data class Frame(val context: GuiGraphicsExtractor, val mouseX: Int, val mouseY: Int)

    private val currentFrame = ThreadLocal<Frame>()

    fun <T> withContext(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, action: Supplier<T>): T {
        val previous = currentFrame.get()
        currentFrame.set(Frame(context, mouseX, mouseY))
        try {
            return action.get()
        } finally {
            if (previous == null) currentFrame.remove() else currentFrame.set(previous)
        }
    }

    internal fun submit(lines: List<Component>, style: Identifier?) {
        val frame = currentFrame.get() ?: return
        if (lines.isEmpty()) return

        frame.context.setTooltipForNextFrame(
            mc.font,
            lines.map { it.visualOrderText },
            Optional.empty(),
            DefaultTooltipPositioner.INSTANCE,
            frame.mouseX,
            frame.mouseY,
            true,
            style,
        )
    }
}
