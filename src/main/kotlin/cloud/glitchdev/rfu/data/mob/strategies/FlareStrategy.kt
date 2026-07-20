package cloud.glitchdev.rfu.data.mob.strategies

import cloud.glitchdev.rfu.data.mob.DeployableManager.Deployable
import cloud.glitchdev.rfu.data.mob.DeployableType
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import net.minecraft.world.item.PlayerHeadItem

class FlareStrategy : DeployableStrategy {
    override val type = DeployableType.FLARE

    private enum class FlareType(val accentLabel: String, val texture: String) {
        SOS("+125%", "ewogICJ0aW1lc3RhbXAiIDogMTY2MjY4Mjc3NjUxNiwKICAicHJvZmlsZUlkIiA6ICI4YjgyM2E1YmU0Njk0YjhiOTE0NmE5MWRhMjk4ZTViNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXBoaXRpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMDA2MmNjOThlYmRhNzJhNmE0Yjg5NzgzYWRjZWYyODE1YjQ4M2EwMWQ3M2VhODdiM2RmNzYwNzJhODlkMTNiIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="),
        ALERT("+50%", "ewogICJ0aW1lc3RhbXAiIDogMTcxOTg1MDQzMTY4MywKICAicHJvZmlsZUlkIiA6ICJmODg2ZDI3YjhjNzU0NjAyODYyYTM1M2NlYmYwZTgwZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb2JpbkdaIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlkMmJmOTg2NDcyMGQ4N2ZkMDZiODRlZmE4MGI3OTVjNDhlZDUzOWIxNjUyM2MzYjFmMTk5MGI0MGMwMDNmNmIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="),
        UNDEFINED("", ""),
    }

    private val seenFlares = HashSet<Int>()
    private var activeFlare: Deployable? = null
    private var foundAnyFlare = false

    override fun resetSession() {
        seenFlares.clear()
        activeFlare = null
        foundAnyFlare = false
    }

    override fun startTick() {
        foundAnyFlare = false
    }

    override fun processEntity(entity: Entity) {
        if (entity !is ArmorStand) {
            if (entity is FireworkRocketEntity) {
                activeFlare = Deployable(
                    type = DeployableType.FLARE,
                    endTimeMillis = System.currentTimeMillis() + 180_000,
                    posX = entity.x,
                    posZ = entity.z,
                    highestY = entity.y,
                )
                foundAnyFlare = true
            }
            return
        }

        val helmet = entity.getItemBySlot(EquipmentSlot.HEAD)
        if (helmet.item !is PlayerHeadItem) return

        val component = helmet[DataComponents.PROFILE] ?: return
        val textures = component.partialProfile().properties["textures"].map { it.value }
        val flareType = FlareType.entries.find { it != FlareType.UNDEFINED && textures.contains(it.texture) }
            ?: return

        val current = activeFlare
        val highestY = maxOf(current?.highestY ?: 0.0, entity.y)

        if (!seenFlares.contains(entity.id)) {
            seenFlares.add(entity.id)
            activeFlare = Deployable(
                type = DeployableType.FLARE,
                endTimeMillis = System.currentTimeMillis() + 180_000,
                accentLabel = flareType.accentLabel,
                posX = entity.x,
                posZ = entity.z,
                highestY = highestY
            )
        } else if (current != null) {
            activeFlare = current.copy(
                highestY = highestY,
                posX = entity.x,
                posZ = entity.z
            )
        }
        foundAnyFlare = true
    }

    override fun getResult(): Deployable? {
        if (!foundAnyFlare) {
            seenFlares.clear()
            activeFlare = null
        }
        return activeFlare
    }
}
