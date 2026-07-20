package cloud.glitchdev.rfu.data.mob.strategies

import cloud.glitchdev.rfu.data.mob.DeployableManager.Deployable
import cloud.glitchdev.rfu.data.mob.DeployableType
import net.minecraft.world.entity.Entity

interface DeployableStrategy {
    val type: DeployableType
    fun resetSession()
    fun startTick()
    fun processEntity(entity: Entity)
    fun getResult(): Deployable?
}
