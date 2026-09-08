package cloud.glitchdev.rfu.utils.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.fishing.Bait
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.constants.fishing.LiquidTypes
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.managers.BaitEventManager
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.utils.World
import net.minecraft.world.phys.Vec3

object SeaCreatureAvailability {
    fun getCurrentContext(): FishingContext? {
        val currentIsland = World.island ?: return null
        val catchHistory = CatchTracker.catchHistory
        var lastHotspot = catchHistory.lastHotspot
        var lastPos = catchHistory.lastPos
        var lastBait = catchHistory.lastBait
        var lastLiquid = catchHistory.lastLiquid

        val player = mc.player
        if (lastPos == Vec3.ZERO && player != null) {
            lastPos = player.position()
            lastHotspot = HotSpotEvents.getHotspotAt(lastPos)
            lastBait = BaitEventManager.lastBait
            lastLiquid = lastHotspot?.liquid
        }

        val bobber = player?.fishing
        if (bobber != null) {
            lastLiquid = when {
                bobber.isInWater -> LiquidTypes.WATER
                bobber.isInLava -> LiquidTypes.LAVA
                else -> lastLiquid
            }
        }

        if (lastLiquid == null && currentIsland.availableLiquids.size == 1) {
            lastLiquid = currentIsland.availableLiquids.first()
        }

        return FishingContext(
            island = currentIsland,
            pos = lastPos,
            hotspot = lastHotspot,
            bait = lastBait,
            liquid = lastLiquid
        )
    }

    fun isAvailable(sc: SeaCreatures, context: FishingContext): Boolean {
        if (context.island == null || !sc.category.islands.contains(context.island)) {
            return false
        }
        if (context.pos != Vec3.ZERO && !sc.condition(context.hotspot, context.pos, context.bait)) {
            return false
        }
        if (context.liquid != null && sc.liquidType != context.liquid) {
            return false
        }
        return true
    }

    fun getAvailableCreatures(
        pool: Collection<SeaCreatures> = SeaCreatures.entries,
        context: FishingContext? = getCurrentContext()
    ): List<SeaCreatures> {
        if (context == null) return emptyList()
        return pool.filter { isAvailable(it, context) }
    }

    data class FishingContext(
        val island: FishingIslands?,
        val pos: Vec3,
        val hotspot: Hotspot?,
        val bait: Bait?,
        val liquid: LiquidTypes?
    )
}
