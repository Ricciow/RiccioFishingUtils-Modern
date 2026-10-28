package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.fishing.FishingIslands
import cloud.glitchdev.rfu.events.managers.BobberLiquidEvents.registerBobberLiquidEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@RFUFeature
object NoChumBucketAlert : Feature {
    private var hasChumBucket = false

    private val bucketPlacedRegex = "You placed down a Chum(?:cap)? Bucket!".toExactRegex()
    private val bucketPickedUpRegex = "Automatically picked up the Chum(?:cap)? Bucket you left back there!".toExactRegex()
    private val bucketCollectedRegex = "You collected your filled Chum(?:cap)? Bucket!".toExactRegex()

    override fun onInitialize() {
        registerGameEvent(bucketPlacedRegex) { _, _, _ ->
            if (World.isInSkyblock) hasChumBucket = true
        }
        registerGameEvent(bucketPickedUpRegex) { _, _, _ ->
            hasChumBucket = false
        }
        registerGameEvent(bucketCollectedRegex) { _, _, _ ->
            hasChumBucket = false
        }
        registerJoinEvent { hasChumBucket = false }
        registerDisconnectEvent { hasChumBucket = false }

        registerBobberLiquidEvent {
            if (!InkFishing.noChumBucket.enabled) return@registerBobberLiquidEvent
            if (!World.isInSkyblock) return@registerBobberLiquidEvent

            if (!hasChumBucket) {
                InkFishing.noChumBucket.show()
            }
        }
    }
}
