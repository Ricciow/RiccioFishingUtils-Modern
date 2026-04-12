package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.DeployablesDisplay
import cloud.glitchdev.rfu.data.mob.DeployableManager
import cloud.glitchdev.rfu.data.mob.DeployableType
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.Title

@RFUFeature
object DeployableTimers : Feature {
    private val previouslyActive = HashMap<DeployableType, DeployableManager.Deployable?>()

    override fun onInitialize() {
        registerJoinEvent {
            previouslyActive.clear()
        }

        registerTickEvent(interval = 20) {
            val active = DeployableManager.getActiveDeployables()

            DeployableType.entries.forEach { type ->
                val prevDeployable = previouslyActive[type]
                val wasActive = prevDeployable != null
                val currentDeployable = active[type]
                val isActive = currentDeployable != null

                if (wasActive && !isActive && alertEnabled(type)) {
                    val shouldAlert = if (type == DeployableType.FLARE) {
                        prevDeployable.accentLabel.isNotEmpty()
                    } else {
                        true
                    }

                    if (shouldAlert) {
                        Title.showTitle(type.expiredTitle)

                        if (GeneralFishing.deployableExpiredSound) {
                            Sounds.playSound("rfu:deployable_expired", 1f, GeneralFishing.deployableExpiredVolume)
                        }
                    }
                }

                previouslyActive[type] = currentDeployable
            }

            DeployablesDisplay.updateDeployables(active)
        }
    }

    private fun alertEnabled(type: DeployableType): Boolean {
        return GeneralFishing.deployableExpiredAlert && GeneralFishing.deployableAlertTypes.contains(type)
    }
}
