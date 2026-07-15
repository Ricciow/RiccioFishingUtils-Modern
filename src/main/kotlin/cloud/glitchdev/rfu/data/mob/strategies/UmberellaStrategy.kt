package cloud.glitchdev.rfu.data.mob.strategies

import cloud.glitchdev.rfu.data.mob.DeployableManager.Deployable
import cloud.glitchdev.rfu.data.mob.DeployableType
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand

class UmberellaStrategy : DeployableStrategy {
    override val type = DeployableType.UMBERELLA

    private val umberellaRegex = """Umberella (\d+)s""".toRegex()
    private var largestUmberellaSeconds: Double? = null
    private var umberellaEntity: ArmorStand? = null

    override fun resetSession() {
        largestUmberellaSeconds = null
        umberellaEntity = null
    }

    override fun startTick() {
        largestUmberellaSeconds = null
        umberellaEntity = null
    }

    override fun processEntity(entity: Entity) {
        if (entity !is ArmorStand) return
        if (!entity.hasCustomName()) return
        val name = entity.name.toUnformattedString()
        val result = umberellaRegex.find(name) ?: return
        val seconds = result.groupValues.getOrNull(1)?.toDoubleOrNull()?.minus(entity.tickCount % 10 * 0.05) ?: return

        if (seconds > (largestUmberellaSeconds ?: 0.0)) {
            largestUmberellaSeconds = seconds
            umberellaEntity = entity
        }
    }

    override fun getResult(): Deployable? {
        val seconds = largestUmberellaSeconds ?: return null
        val entity = umberellaEntity ?: return null
        return Deployable(
            type = DeployableType.UMBERELLA,
            endTimeMillis = System.currentTimeMillis() + (seconds * 1_000).toLong(),
            posX = entity.x,
            posZ = entity.z,
            highestY = entity.y,
        )
    }
}
