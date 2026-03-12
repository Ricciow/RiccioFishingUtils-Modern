package cloud.glitchdev.rfu.gui.hud.elements

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor.AQUAMARINE
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.gui.hud.AbstractTextHudElement
import cloud.glitchdev.rfu.gui.hud.HudElement
import cloud.glitchdev.rfu.data.mob.DeployableManager
import cloud.glitchdev.rfu.data.mob.DeployableType
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@HudElement
object DeployablesDisplay : AbstractTextHudElement("deployablesDisplay") {
    private var activeDeployables: Map<DeployableType, DeployableManager.Deployable> = emptyMap()

    override val enabled: Boolean
        get() = activeDeployables.any { (type, _) -> type.isDisplayEnabled() } || super.enabled

    override fun onUpdateState() {
        super.onUpdateState()

        val now = System.currentTimeMillis()
        val lines = DeployableType.entries
            .filter { it.isDisplayEnabled() }
            .mapNotNull { type ->
                val deployable = activeDeployables[type]
                buildLine(type, deployable, now)
            }

        text.setText(if (lines.isEmpty()) "deployablesDisplay" else lines.joinToString("\n"))
    }

    fun updateDeployables(active: Map<DeployableType, DeployableManager.Deployable>) {
        this.activeDeployables = active
        updateState()
    }

    private fun buildLine(type: DeployableType, deployable: DeployableManager.Deployable?, now: Long): String? {
        val label = "${type.labelColor}${BOLD}${type.displayName}:"
        if(deployable == null) {
            return if(isEditing && (activeDeployables.isEmpty() || activeDeployables.all { (_, value) -> !value.type.isDisplayEnabled() })) {
                "$label ${YELLOW}0s"
            } else {
                null
            }
        }

        val remainingMillis = deployable.endTimeMillis - now
        val remaining: Duration = if (remainingMillis > 0) remainingMillis.milliseconds else Duration.ZERO

        return buildString {
            append(label)
            if (remaining == Duration.ZERO) {
                append(" ${YELLOW}Soon")
            } else {
                append(" $YELLOW${remaining.toReadableString()}")
            }
            if (deployable.accentLabel.isNotEmpty()) {
                append(" $AQUAMARINE${deployable.accentLabel}")
            }
        }
    }

    private fun DeployableType.isDisplayEnabled(): Boolean =
        GeneralFishing.deployableTimerDisplay.contains(this)
}
