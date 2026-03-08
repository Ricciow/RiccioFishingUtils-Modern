package cloud.glitchdev.rfu.data.mob

import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import net.minecraft.world.item.PlayerHeadItem

@AutoRegister
object DeployableManager : RegisteredEvent {

    data class Deployable(
        val type: DeployableType,
        val endTimeMillis: Long,
        val accentLabel: String = "",
    )

    private enum class FlareType(val accentLabel: String, val texture: String) {
        SOS("+125%", "ewogICJ0aW1lc3RhbXAiIDogMTY2MjY4Mjc3NjUxNiwKICAicHJvZmlsZUlkIiA6ICI4YjgyM2E1YmU0Njk0YjhiOTE0NmE5MWRhMjk4ZTViNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXBoaXRpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMDA2MmNjOThlYmRhNzJhNmE0Yjg5NzgzYWRjZWYyODE1YjQ4M2EwMWQ3M2VhODdiM2RmNzYwNzJhODlkMTNiIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="),
        ALERT("+50%", "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1MDQzMTY4MywKICAicHJvZmlsZUlkIiA6ICJmODg2ZDI3YjhjNzU0NjAyODYyYTM1M2NlYmYwZTgwZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb2JpbkdaIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlkMmJmOTg2NDcyMGQ4N2ZkMDZiODRlZmE4MGI3OTVjNDhlZDUzOWIxNjUyM2MzYjFmMTk5MGI0MGMwMDNmNmIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="),
        UNDEFINED("", ""),
    }

    private val activeDeployables = HashMap<DeployableType, Deployable>()
    private val seenFlares = HashSet<Int>()

    private val umberellaRegex = """Umberella (\d+)s""".toRegex()

    override fun register() {
        registerTickEvent(0, 10) { client ->
            val world = client.level ?: return@registerTickEvent
            update(world)
        }

        registerJoinEvent {
            clearAll()
        }

        registerGameEvent("Your flare disappeared because you were too far away!".toExactRegex()) { _, _, _ ->
            resetFlare()
        }
    }

    fun getActiveDeployable(type: DeployableType): Deployable? = activeDeployables[type]

    fun getActiveDeployables(): Map<DeployableType, Deployable> = activeDeployables.toMap()

    fun update(world: ClientLevel) {
        var foundAnyFlare = false
        var largestUmberellaSeconds: Double? = null

        world.entitiesForRendering().forEach { entity ->
            if (entity is ArmorStand) {
                val umbSeconds = checkUmberella(entity)
                if (umbSeconds != null && umbSeconds > (largestUmberellaSeconds ?: 0.0)) {
                    largestUmberellaSeconds = umbSeconds
                }
            }

            if (checkFlare(entity)) {
                foundAnyFlare = true
            }
        }

        if (!foundAnyFlare) {
            resetFlare()
        }

        if (largestUmberellaSeconds == null) {
            resetUmberella()
        } else {
            activeDeployables[DeployableType.UMBERELLA] = Deployable(
                type = DeployableType.UMBERELLA,
                endTimeMillis = System.currentTimeMillis() + (largestUmberellaSeconds * 1_000).toLong(),
            )
        }
    }

    fun resetFlare() {
        seenFlares.clear()
        activeDeployables.remove(DeployableType.FLARE)
    }

    private fun resetUmberella() {
        activeDeployables.remove(DeployableType.UMBERELLA)
    }

    private fun clearAll() {
        seenFlares.clear()
        activeDeployables.clear()
    }

    private fun checkUmberella(entity: ArmorStand): Double? {
        if (!entity.hasCustomName()) return null
        val name = entity.name.toUnformattedString()
        val result = umberellaRegex.find(name) ?: return null
        return result.groupValues.getOrNull(1)?.toDoubleOrNull()?.minus(entity.tickCount % 10 * 0.05)
    }

    private fun checkFlare(entity: net.minecraft.world.entity.Entity): Boolean {
        if (entity !is ArmorStand) {
            if (entity is FireworkRocketEntity) {
                activeDeployables[DeployableType.FLARE] = Deployable(
                    type = DeployableType.FLARE,
                    endTimeMillis = System.currentTimeMillis() + 180_000,
                )
                return true
            }
            return false
        }

        val helmet = entity.getItemBySlot(EquipmentSlot.HEAD)
        if (helmet.item !is PlayerHeadItem) return false

        val component = helmet[DataComponents.PROFILE] ?: return false
        val textures = component.partialProfile().properties["textures"].map { it.value }
        val flareType = FlareType.entries.find { it != FlareType.UNDEFINED && textures.contains(it.texture) }
            ?: return false

        if (!seenFlares.contains(entity.id)) {
            seenFlares.add(entity.id)
            activeDeployables[DeployableType.FLARE] = Deployable(
                type = DeployableType.FLARE,
                endTimeMillis = System.currentTimeMillis() + 180_000,
                accentLabel = flareType.accentLabel,
            )
        }
        return true
    }
}