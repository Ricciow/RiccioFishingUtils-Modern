package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.item.ItemStack

object ContainerEvents : AbstractEventManager<(containerId: Int, itens: List<ItemStack>) -> Unit, ContainerEvents.ContainerOpenEvent>() {
    override val runTasks: (Int, List<ItemStack>) -> Unit = { containerId, itens ->
        safeExecution {
            //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
            val containerName = mc.gui.screen()?.title?.toUnformattedString() ?: ""
            //~}
            tasks.forEach { event -> event.userCallback(containerName, containerId, itens) }
        }
    }

    fun registerContainerOpenEvent(
        priority: Int = 20,
        callback: (containerName: String, containerId: Int, itens: List<ItemStack>) -> Unit
    ): ContainerOpenEvent {
        return ContainerOpenEvent(priority, callback).register()
    }


    class ContainerOpenEvent(
        priority: Int = 20,
        val userCallback: (containerName: String, containerId: Int, itens: List<ItemStack>) -> Unit
    ) : ManagedTask<(containerId: Int, itens: List<ItemStack>) -> Unit, ContainerOpenEvent>(priority, { _, _ -> }) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}