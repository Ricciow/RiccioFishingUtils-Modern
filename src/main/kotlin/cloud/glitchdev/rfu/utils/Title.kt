package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.network.chat.Component

object Title {
    private val queue : MutableList<TitleObj> = ArrayDeque()
    private var isRunning = false

    fun showTitle(title: String, subTitle : String = "", condition: () -> Boolean = { true }) {
        queue.add(TitleObj(title, subTitle, condition))
        displayTitles()
    }

    fun displayTitles() {
        if(isRunning) return
        isRunning = true
        CoroutineScope(Dispatchers.Default).launch {
            while(queue.isNotEmpty()) {
                val title = queue.removeFirst()
                if(title.condition()) {
                    mc.gui.setTitle(Component.literal(title.title))
                    mc.gui.setSubtitle(Component.literal(title.subTitle))
                    delay(5000)
                }
            }
            isRunning = false
        }
    }

    data class TitleObj(
        val title : String,
        val subTitle: String,
        val condition : () -> Boolean = { true }
    )
}