package cloud.glitchdev.rfu.data.fishing

import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

data class BobberInfo(
    val entityId: Int,
    var ownerName: String?,
    var ownerUUID: UUID?,
    var lastPos: Vec3,
    val spawnTime: Instant = Clock.System.now(),
    var removedTime: Instant? = null
)
