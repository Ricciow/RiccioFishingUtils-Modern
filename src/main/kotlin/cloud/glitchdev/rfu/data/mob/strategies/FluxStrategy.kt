package cloud.glitchdev.rfu.data.mob.strategies

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.mob.DeployableManager.Deployable
import cloud.glitchdev.rfu.data.mob.DeployableType
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand

class FluxStrategy : DeployableStrategy {
    override val type = DeployableType.FLUX

    private val fluxRegex = """(?i)(Mana\s?Flux|Overflux|Plasmaflux) (\d+)s""".toRegex()

    private data class FluxInfo(
        val name: String,
        val accentLabel: String,
        val range: Double,
        val labelColor: TextColor,
    )

    private var largestFluxSeconds: Double? = null
    private var fluxEntity: ArmorStand? = null
    private var activeFluxInfo: FluxInfo? = null

    private fun getFluxInfo(matchedName: String): FluxInfo? {
        val lower = matchedName.lowercase()
        return when {
            lower.contains("plasmaflux") -> FluxInfo("Plasmaflux", "+125%", 20.0, TextColor.MAGENTA)
            lower.contains("overflux") -> FluxInfo("Overflux", "+100%", 18.0, TextColor.LIGHT_RED)
            lower.contains("mana flux") || lower.contains("manaflux") -> FluxInfo("Mana Flux", "+50%", 18.0, TextColor.AQUAMARINE)
            else -> null
        }
    }

    override fun resetSession() {
        largestFluxSeconds = null
        fluxEntity = null
        activeFluxInfo = null
    }

    override fun startTick() {
        largestFluxSeconds = null
        fluxEntity = null
        activeFluxInfo = null
    }

    override fun processEntity(entity: Entity) {
        if (entity !is ArmorStand) return
        if (!entity.hasCustomName()) return
        val name = entity.name.toUnformattedString()
        val result = fluxRegex.find(name) ?: return
        val matchedName = result.groupValues.getOrNull(1) ?: return
        val seconds = result.groupValues.getOrNull(2)?.toDoubleOrNull()?.minus(entity.tickCount % 10 * 0.05) ?: return
        val info = getFluxInfo(matchedName) ?: return

        if (seconds > (largestFluxSeconds ?: 0.0)) {
            largestFluxSeconds = seconds
            fluxEntity = entity
            activeFluxInfo = info
        }
    }

    override fun getResult(): Deployable? {
        val seconds = largestFluxSeconds ?: return null
        val entity = fluxEntity ?: return null
        val info = activeFluxInfo ?: return null
        return Deployable(
            type = DeployableType.FLUX,
            endTimeMillis = System.currentTimeMillis() + (seconds * 1_000).toLong(),
            accentLabel = info.accentLabel,
            posX = entity.x,
            posZ = entity.z,
            highestY = entity.y,
            customName = info.name,
            labelColorOverride = info.labelColor,
            rangeOverride = info.range,
        )
    }
}
