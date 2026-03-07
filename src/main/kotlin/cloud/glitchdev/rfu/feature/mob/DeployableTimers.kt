package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.DeployablesDisplay
import cloud.glitchdev.rfu.manager.mob.DeployableManager
import cloud.glitchdev.rfu.manager.mob.DeployableType
import cloud.glitchdev.rfu.utils.Title

@RFUFeature
object DeployableTimers : Feature {
    private val previouslyActive = HashMap<DeployableType, Boolean>()

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            val active = DeployableManager.getActiveDeployables()

            DeployableType.entries.forEach { type ->
                val wasActive = previouslyActive[type] ?: false
                val isActive = active.containsKey(type)

                if (wasActive && !isActive && alertEnabled(type)) {
                    Title.showTitle(type.expiredTitle)
                }

                previouslyActive[type] = isActive
            }

            DeployablesDisplay.updateDeployables(active)
        }
    }

    private fun alertEnabled(type: DeployableType): Boolean {
        return GeneralFishing.deployableExpiredAlert && GeneralFishing.deployableAlertTypes.contains(type)
    }
}
