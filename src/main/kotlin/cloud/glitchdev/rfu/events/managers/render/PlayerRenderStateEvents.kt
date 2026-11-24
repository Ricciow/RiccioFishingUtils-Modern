package cloud.glitchdev.rfu.events.managers.render

import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.EventManager
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.world.entity.Avatar

@EventManager(register = false)
object PlayerRenderStateEvents : AbstractEventManager<(Avatar, AvatarRenderState) -> Unit, PlayerRenderStateEvents.PlayerRenderStateEvent>() {
    override val runTasks: (Avatar, AvatarRenderState) -> Unit = { player, state ->
        safeExecution(mainThread = false) {
            tasks.forEach { it.callback(player, state) }
        }
    }

    class PlayerRenderStateEvent(
        priority: Int,
        callback: (Avatar, AvatarRenderState) -> Unit
    ) : ManagedTask<(Avatar, AvatarRenderState) -> Unit, PlayerRenderStateEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}