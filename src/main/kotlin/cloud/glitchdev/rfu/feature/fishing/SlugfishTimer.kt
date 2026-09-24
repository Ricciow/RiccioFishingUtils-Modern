package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.TrophyFishing
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.PetEvents.registerPetUpdateEvent
import cloud.glitchdev.rfu.events.managers.RenderEvents.registerRenderEvent
import cloud.glitchdev.rfu.events.managers.ServerTickEvents
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import cloud.glitchdev.rfu.utils.rendering.Render3D
import cloud.glitchdev.rfu.utils.rendering.Render3DBuilder.Companion.text
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@RFUFeature
object SlugfishTimer : Feature {
    private var slugPetReduction = 1.0
    private const val SLUG_PET_INCREMENT = 49.0/99
    private var startServerTick = 0L
    private var wasFishing = false

    override fun onInitialize() {
        registerPetUpdateEvent { _, petName, petLevel ->
            if (petName == "Slug") {
                val level = petLevel ?: 1
                slugPetReduction = 1 - (1 + SLUG_PET_INCREMENT * (level - 1)) / 100
            } else {
                slugPetReduction = 1.0
            }
        }

        registerTickEvent {
            if(!TrophyFishing.slugfishTimer) return@registerTickEvent
            if (!FishingSession.isTrophyFishing) return@registerTickEvent
            if (mc.player?.fishing != null) {
                if(wasFishing) return@registerTickEvent
                startServerTick = ServerTickEvents.currentServerTick
                wasFishing = true
            } else {
                wasFishing = false
            }

        }

        registerRenderEvent { renderContext ->
            if(!TrophyFishing.slugfishTimer) return@registerRenderEvent
            if(!FishingSession.isTrophyFishing) return@registerRenderEvent
            val bobber = mc.player?.fishing ?: return@registerRenderEvent
            val elapsedTicks = (ServerTickEvents.currentServerTick - startServerTick).coerceAtLeast(0L)
            val duration = (elapsedTicks * 50L).milliseconds
            Render3D.draw(renderContext) {
                val color = if(duration < 20.seconds * slugPetReduction) {
                    TextColor.LIGHT_RED
                } else {
                    TextColor.LIGHT_GREEN
                }
                text {
                    text = "$color${duration.toReadableString(true)}"
                    location = bobber.position().add(0.0, 0.5, 0.0)
                }
            }
        }
    }
}
