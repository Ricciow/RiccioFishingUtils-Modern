package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import kotlinx.coroutines.*
import net.minecraft.network.chat.Component

object Title {
    private val queue: ArrayDeque<TitleObj> = ArrayDeque()
    private var isRunning = false

    fun showTitle(title: String, subTitle: String = "", fadeIn: Int = 5, duration: Int = 10, fadeOut: Int = 5, condition: () -> Boolean = { true }) {
        queue.add(TitleObj(title, subTitle, fadeIn, duration, fadeOut, condition))
        displayTitles()
    }

    fun displayTitles() {
        if (isRunning) return
        isRunning = true

        CoroutineScope(Dispatchers.Default).launch {
            while (queue.isNotEmpty()) {
                val title = queue.removeFirst()

                if (title.condition()) {
                    mc.execute {
                        mc.gui.setTimes(title.fadeIn, title.duration, title.fadeOut)
                        mc.gui.setTitle(Component.literal(title.title))
                        mc.gui.setSubtitle(Component.literal(title.subTitle))
                    }

                    val totalTicks = title.fadeIn + title.duration + title.fadeOut
                    delay(totalTicks * 50L)
                }
            }
            isRunning = false
        }
    }

    data class TitleObj(
        val title: String,
        val subTitle: String,
        val fadeIn: Int = 2,
        val duration: Int = 10,
        val fadeOut: Int = 2,
        val condition: () -> Boolean = { true }
    )
}