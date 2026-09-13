package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.AbstractEventManager
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult

@AutoRegister
object EntityInteractEvents : AbstractEventManager<(player: Player, world: Level, hand: InteractionHand, entity: Entity, hitResult: EntityHitResult?) -> Unit, EntityInteractEvents.EntityInteractEvent>(), RegisteredEvent {

    override fun register() {
        UseEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            if (player == mc.player && world.isClientSide) {
                runTasks(player, world, hand, entity, hitResult)
            }

            InteractionResult.PASS
        }
    }

    override val runTasks: (Player, Level, InteractionHand, Entity, EntityHitResult?) -> Unit = { player, world, hand, entity, hitResult ->
        safeExecution {
            tasks.forEach { task -> task.callback(player, world, hand, entity, hitResult) }
        }
    }

    fun registerEntityInteractEvent(
        priority: Int = 20,
        callback: (player: Player, world: Level, hand: InteractionHand, entity: Entity, hitResult: EntityHitResult?) -> Unit
    ): EntityInteractEvent {
        return EntityInteractEvent(priority, callback).register()
    }

    fun registerEntityInteractEvent(
        priority: Int = 20,
        mainHandOnly: Boolean = true,
        callback: (entity: Entity) -> Unit
    ): EntityInteractEvent {
        return registerEntityInteractEvent(priority) { _, _, hand, entity, _ ->
            if (!mainHandOnly || hand == InteractionHand.MAIN_HAND) {
                callback(entity)
            }
        }
    }

    class EntityInteractEvent(
        priority: Int = 20,
        callback: (player: Player, world: Level, hand: InteractionHand, entity: Entity, hitResult: EntityHitResult?) -> Unit
    ) : ManagedTask<(player: Player, world: Level, hand: InteractionHand, entity: Entity, hitResult: EntityHitResult?) -> Unit, EntityInteractEvent>(priority, callback) {
        override fun register() = submitTask(this)
        override fun unregister() = removeTask(this)
    }
}
