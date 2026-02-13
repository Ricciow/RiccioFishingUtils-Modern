package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.minecraft.world.item.ItemStack

object ContainerEvents : AbstractEventManager<(containerId : Int, itens : List<ItemStack>) -> Unit, ContainerEvents.ContainerOpenEvent>(), RegisteredEvent {
    override fun register() {}

    fun runTasks(containerId : Int, itens : List<ItemStack>) {
        tasks.forEach { event -> event.callback(containerId, itens) }
    }

    fun registerContainerOpenEvent(
        priority: Int = 20,
        callback: (containerId : Int, itens : List<ItemStack>) -> Unit
    ): ContainerOpenEvent {
        return ContainerOpenEvent(priority, callback).register()
    }


    class ContainerOpenEvent(
        priority: Int = 20,
        callback: (containerId : Int, itens : List<ItemStack>) -> Unit
    ) : ManagedTask<(containerId : Int, itens : List<ItemStack>) -> Unit, ContainerOpenEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}