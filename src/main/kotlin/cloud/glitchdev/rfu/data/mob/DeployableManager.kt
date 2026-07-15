package cloud.glitchdev.rfu.data.mob

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.mob.strategies.FlareStrategy
import cloud.glitchdev.rfu.data.mob.strategies.FluxStrategy
import cloud.glitchdev.rfu.data.mob.strategies.UmberellaStrategy
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.round

@AutoRegister
object DeployableManager : RegisteredEvent {
    data class Deployable(
        val type: DeployableType,
        val endTimeMillis: Long,
        val accentLabel: String = "",
        val posX: Double? = null,
        val posZ: Double? = null,
        val highestY: Double? = null,
        val customName: String = "",
        val labelColorOverride: TextColor? = null,
        val rangeOverride: Double? = null,
    ) {
        fun isInRange(playerPos: Vec3): Boolean {
            if (posX == null || posZ == null || highestY == null) return true
            val dy = playerPos.y - (round(highestY * 2.0) / 2.0 + 0.35)
            val dx = playerPos.x - posX
            val dz = playerPos.z - posZ
            val r = rangeOverride ?: type.range
            return dx * dx + dy * dy + dz * dz <= r * r
        }
    }

    private val activeDeployables = HashMap<DeployableType, Deployable>()

    private val strategies = listOf(
        FlareStrategy(),
        UmberellaStrategy(),
        FluxStrategy()
    )

    override fun register() {
        registerTickEvent(0, 10) { client ->
            val world = client.level ?: return@registerTickEvent
            update(world)
        }

        registerJoinEvent {
            clearAll()
        }
    }

    fun getActiveDeployable(type: DeployableType): Deployable? = activeDeployables[type]

    fun getActiveDeployables(): Map<DeployableType, Deployable> = activeDeployables.toMap()

    fun update(world: ClientLevel) {
        strategies.forEach { it.startTick() }

        world.entitiesForRendering().forEach { entity ->
            strategies.forEach { strategy ->
                strategy.processEntity(entity)
            }
        }

        strategies.forEach { strategy ->
            val result = strategy.getResult()
            if (result != null) {
                activeDeployables[strategy.type] = result
            } else {
                activeDeployables.remove(strategy.type)
            }
        }
    }

    private fun clearAll() {
        strategies.forEach { it.resetSession() }
        activeDeployables.clear()
    }
}