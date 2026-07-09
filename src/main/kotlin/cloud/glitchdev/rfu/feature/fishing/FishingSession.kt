package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.data.fishing.Hotspot
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.events.managers.KeyboardEvents.registerKeyboardEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.events.managers.SkillEvents.registerSkillXpUpdateEvent
import cloud.glitchdev.rfu.utils.SkillTracker
import cloud.glitchdev.rfu.constants.skyblock.SkillType
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.ink.InkSessionTracker
import cloud.glitchdev.rfu.utils.SlidingRateTracker
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.command.SimpleCommand
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@RFUFeature
object FishingSession : Feature {

    val xpTracker = SlidingRateTracker()
    val scTracker = SlidingRateTracker()
    val inkTracker = SlidingRateTracker()
    var lastHotspot : Hotspot? = null

    val isHotspotFishing : Boolean
        get() = lastHotspot != null

    private var totalFishingXp: Long = 0L

    var startFishing: Instant = Instant.DISTANT_PAST
        private set

    var lastFishingEvent: Instant = Instant.DISTANT_PAST
        private set

    var pausedAt: Instant? = null
        private set

    val isFishing: Boolean
        get() = startFishing != Instant.DISTANT_PAST

    val isPaused: Boolean
        get() = pausedAt != null

    val pausedDuration: Duration
        get() = pausedAt?.let { Clock.System.now() - it } ?: Duration.ZERO

    val duration: Duration
        get() {
            if (!isFishing) return Duration.ZERO
            return ((pausedAt ?: Clock.System.now()) - startFishing).coerceAtLeast(Duration.ZERO)
        }

    override fun onInitialize() {
        registerSkillXpUpdateEvent(SkillType.FISHING) { _, xp ->
            handleActivity()
            if (totalFishingXp != 0L && xp > totalFishingXp) {
                val diff = xp - totalFishingXp
                if (diff < 1_000_000L) {
                    xpTracker.addEvent(diff.toDouble())
                }
            }
            totalFishingXp = xp
        }

        totalFishingXp = SkillTracker.getSkillXp(SkillType.FISHING)

        registerSeaCreatureCatchEvent { _, isDoubleHook, hotspot, _, _ ->
            handleActivity()
            lastHotspot = hotspot
            scTracker.addEvent(if (isDoubleHook) 2.0 else 1.0)
        }

        registerLocationEvent {
            lastHotspot = null
        }

        registerKeyboardEvent({ GeneralFishing.pauseKeybind }, onPress = { togglePause() })

        registerTickEvent(interval = 20) {
            val now = Clock.System.now()
            val limit = GeneralFishing.fishingTime.minutes

            if (isFishing && pausedAt == null && (now - lastFishingEvent) > limit) {
                if (GeneralFishing.pauseSessionOnWindowReached) {
                    pausedAt = lastFishingEvent
                } else {
                    resetSession()
                }
            }
            
            xpTracker.update()
            scTracker.update()
            inkTracker.update()
        }
    }

    fun togglePause() {
        if (!isFishing) return
        if (isPaused) handleActivity() else pausedAt = Clock.System.now()
    }

    fun handleActivity() {
        val now = Clock.System.now()
        if (startFishing == Instant.DISTANT_PAST) {
            startFishing = now
        } else {
            pausedAt?.let {
                val pausedDuration = now - it
                startFishing += pausedDuration
                scTracker.updateLastEvent(now)
                xpTracker.updateLastEvent(now)
                inkTracker.updateLastEvent(now)
            }
            pausedAt = null
        }
        lastFishingEvent = now
    }

    fun resetSession() {
        startFishing = Instant.DISTANT_PAST
        lastFishingEvent = Instant.DISTANT_PAST
        pausedAt = null
        totalFishingXp = SkillTracker.getSkillXp(SkillType.FISHING)

        scTracker.reset()
        scTracker.update()

        InkSessionTracker.resetSession()
        inkTracker.reset()
        inkTracker.update()

        xpTracker.reset()
        xpTracker.update()
    }

    @Command
    object ResetCommand : SimpleCommand("rfuresetsession") {
        override val description: String = "Resets your current fishing session."
        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            resetSession()
            context.source.sendFeedback(
                TextUtils.rfuLiteral("Your fishing session has been reset!", TextStyle(TextColor.LIGHT_GREEN))
            )
            return 1
        }
    }

    @Command
    object ResetXpCommand : SimpleCommand("rfuresetxph") {
        override val description: String = "Resets your current Xp/h tracker."
        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            xpTracker.reset()
            totalFishingXp = SkillTracker.getSkillXp(SkillType.FISHING)
            context.source.sendFeedback(
                TextUtils.rfuLiteral("The Xp/h tracker has been reset!", TextStyle(TextColor.LIGHT_GREEN))
            )
            return 1
        }
    }

    @Command
    object ResetScCommand : SimpleCommand("rfuresetsch") {
        override val description: String = "Resets your current SC/h tracker."
        override fun execute(context: CommandContext<FabricClientCommandSource>): Int {
            scTracker.reset()
            context.source.sendFeedback(
                TextUtils.rfuLiteral("The SC/h tracker has been reset!", TextStyle(TextColor.LIGHT_GREEN))
            )
            return 1
        }
    }
}
