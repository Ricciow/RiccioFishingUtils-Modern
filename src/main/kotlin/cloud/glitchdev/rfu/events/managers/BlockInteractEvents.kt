package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult

@AutoRegister
object BlockInteractEvents : AbstractEventManager<(BlockPos) -> Unit, BlockInteractEvents.BlockInteractEvent>(), RegisteredEvent {
    override fun register() {
        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            if (player == mc.player && world.isClientSide && hand == InteractionHand.MAIN_HAND) {
                runTasks(hitResult.blockPos)
            }
            InteractionResult.PASS
        }
    }

    override val runTasks: (BlockPos) -> Unit = { pos ->
        safeExecution {
            tasks.forEach { it.callback(pos) }
        }
    }

    fun registerBlockInteractEvent(priority: Int = 20, callback: (BlockPos) -> Unit): BlockInteractEvent {
        return BlockInteractEvent(priority, callback).register()
    }

    class BlockInteractEvent(priority: Int = 20, callback: (BlockPos) -> Unit) :
        ManagedTask<(BlockPos) -> Unit, BlockInteractEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
