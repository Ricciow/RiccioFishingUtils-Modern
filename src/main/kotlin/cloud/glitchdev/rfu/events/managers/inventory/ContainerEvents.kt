package cloud.glitchdev.rfu.events.managers.inventory

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.item.ItemStack

@EventManager(register = false)
object ContainerEvents : AbstractEventManager<(containerId: Int, itens: List<ItemStack>) -> Unit, ContainerEvents.ContainerOpenEvent>() {
    override val runTasks: (Int, List<ItemStack>) -> Unit = { containerId, itens ->
        safeExecution {
            //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
            val containerName = mc.gui.screen()?.title?.toUnformattedString() ?: ""
            //~}
            tasks.forEach { event -> event.userCallback(containerName, containerId, itens) }
        }
    }

    class ContainerOpenEvent(
        priority: Int = DEFAULT_EVENT_PRIORITY,
        callback: (containerName: String, containerId: Int, itens: List<ItemStack>) -> Unit
    ) : ManagedTask<(containerId: Int, itens: List<ItemStack>) -> Unit, ContainerOpenEvent>(priority, { _, _ -> }) {
        val userCallback = callback

        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
