package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.registerBobberLiquidEvent
import cloud.glitchdev.rfu.events.managers.registerGameEvent
import cloud.glitchdev.rfu.events.managers.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.registerJoinEvent
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

    override fun onInitialize(scope: EventScope) {
        scope.registerGameEvent(bucketPlacedRegex) { _, _, _ ->
            if (World.isInSkyblock) hasChumBucket = true
        }
        scope.registerGameEvent(bucketPickedUpRegex) { _, _, _ ->
            hasChumBucket = false
        }
        scope.registerGameEvent(bucketCollectedRegex) { _, _, _ ->
            hasChumBucket = false
        }
        scope.registerJoinEvent { hasChumBucket = false }
        scope.registerDisconnectEvent { hasChumBucket = false }

        scope.registerBobberLiquidEvent {
            if (!InkFishing.noChumBucket.enabled) return@registerBobberLiquidEvent
            if (!World.isInSkyblock) return@registerBobberLiquidEvent

            if (!hasChumBucket) {
                InkFishing.noChumBucket.show()
            }
        }
    }
}
