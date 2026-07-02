package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.LotusAtollSettings
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.text
import net.minecraft.world.entity.Display
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3
import java.awt.Color
import kotlin.math.abs
import kotlin.math.roundToInt

@RFUFeature
object LilypadSizeDisplayFeature : Feature {
    private val trackedLilypads = mutableMapOf<Int, LilypadState>()

    private class LilypadState(
        var lastPos: Vec3,
        var lastScale: Float,
        var lastChangeTime: Long
    )

    override fun onInitialize() {
        registerRenderEvent { context ->
            if (!LotusAtollSettings.lilypadSizeDisplay) return@registerRenderEvent
            if (World.island != FishingIslands.ATOLL) return@registerRenderEvent

            val world = mc.level ?: return@registerRenderEvent
            val allEntities = world.entitiesForRendering()

            val lilypads = allEntities.filterIsInstance<Display.ItemDisplay>()
                .filter { it.itemStack.item == Items.LILY_PAD }

            if (lilypads.isEmpty()) {
                trackedLilypads.clear()
                return@registerRenderEvent
            }

            val currentIds = lilypads.map { it.id }.toSet()
            trackedLilypads.keys.retainAll(currentIds)

            Render3D.draw(context) {
                val now = System.currentTimeMillis()
                val tickDelta = mc.deltaTracker.getGameTimeDeltaPartialTick(true)

                for (entity in lilypads) {
                    val currentPos = entity.position()
                    val renderState = entity.renderState() ?: continue
                    val scaleVec = renderState.transformation().get(1.0f).scale()
                    val currentScale = scaleVec.x()

                    var state = trackedLilypads[entity.id]
                    if (state == null) {
                        state = LilypadState(currentPos, currentScale, 0L)
                        trackedLilypads[entity.id] = state
                    } else {
                        val posChanged = currentPos.distanceToSqr(state.lastPos) > 0.0001
                        val scaleChanged = abs(currentScale - state.lastScale) > 0.0001
                        if (posChanged || scaleChanged) {
                            state.lastPos = currentPos
                            state.lastScale = currentScale
                            state.lastChangeTime = now
                        }
                    }

                    if (now - state.lastChangeTime < 5000L) {
                        val percentage = ((currentScale - 0.6f) / (8.0f - 0.6f) * 100f).coerceIn(0f, 100f)
                        val percentageText = "§a${percentage.roundToInt()}%"

                        val entityPos = entity.getPosition(tickDelta)
                        val textLoc = entityPos.add(0.0, entity.bbHeight.toDouble() + 0.5, 0.0)

                        text {
                            location = textLoc
                            text = percentageText
                            color = Color.WHITE
                            scale = 0.025f
                            seeThrough = false
                            backgroundOpacity = 0.4f
                        }
                    }
                }
            }
        }
    }
}
