package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.CustomBinds
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig.RARE_SC_REGEX
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.mob.MobManager
import cloud.glitchdev.rfu.events.keybind.KeyContext
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerDisconnectEvent
import cloud.glitchdev.rfu.events.managers.ItemUsedEvents.registerItemUsedEvent
import cloud.glitchdev.rfu.events.managers.KeybindEvents.registerKeybind
import cloud.glitchdev.rfu.events.managers.OptionsSaveEvents.registerAfterOptionsSave
import cloud.glitchdev.rfu.events.managers.OptionsSaveEvents.registerBeforeOptionsSave
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.isFishingRod
import cloud.glitchdev.rfu.utils.dsl.rfuKey
import cloud.glitchdev.rfu.utils.dsl.toInputKey
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping

@RFUFeature
object FishingKeybindsHandler : Feature {
    private var lastCastTime = 0L
    var isTemporarilySuspended = false
        private set
    var isApplied = false
        private set

    private var wasSavedWhileApplied = false
    private val originalKeys = mutableMapOf<KeyMapping, InputConstants.Key>()

    override fun onInitialize() {
        registerKeybind {
            key = { CustomBinds.resetBindsKeybind }
            priority = 100
            context = KeyContext.ANY
            onPress = {
                if (GeneralFishing.overrideFishingKeybinds && !isTemporarilySuspended) {
                    isTemporarilySuspended = true
                    updateState()
                    Chat.sendMessage(TextUtils.rfuLiteral("Disabled custom fishing keybinds", TextColor.LIGHT_GREEN))
                }
            }
        }

        registerItemUsedEvent { item ->
            if (item.isFishingRod()) {
                lastCastTime = System.currentTimeMillis()
                updateState()
            }
        }

        registerSeaCreatureCatchEvent { _, _, _, _, _ ->
            if (isTemporarilySuspended) {
                isTemporarilySuspended = false
                updateState()
            }
        }

        registerDisconnectEvent {
            isTemporarilySuspended = false
            revertKeybinds()
        }

        registerShutdownEvent {
            revertKeybinds()
        }

        registerBeforeOptionsSave {
            if (isApplied) {
                wasSavedWhileApplied = true
                revertKeybinds()
            }
        }

        registerAfterOptionsSave {
            if (wasSavedWhileApplied) {
                wasSavedWhileApplied = false
                updateState()
            }
        }

        registerTickEvent(interval = 1) {
            updateState()
        }
    }

    fun isOverriding(): Boolean {
        if (!GeneralFishing.overrideFishingKeybinds) return false
        if (isTemporarilySuspended) return false
        mc.player ?: return false

        //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
        val screen = mc.gui.screen()
        //~}
        if (screen != null) return false

        if (GeneralFishing.disableOnRareSC && MobManager.getEntities().any { RARE_SC_REGEX.matches(it.sbName) }) {
            return false
        }

        if (FishingSession.isFishing && (System.currentTimeMillis() - lastCastTime < 30_000)) {
            return true
        }

        return false
    }

    @Synchronized
    fun updateState() {
        val shouldOverride = isOverriding()
        if (shouldOverride && !isApplied) {
            applyKeybinds()
        } else if (!shouldOverride && isApplied) {
            revertKeybinds()
        }
    }

    @Synchronized
    fun applyKeybinds() {
        if (isApplied) return
        val options = mc.options

        originalKeys.clear()

        val customHotbars = arrayOf(
            CustomBinds.fishingHotbar1,
            CustomBinds.fishingHotbar2,
            CustomBinds.fishingHotbar3,
            CustomBinds.fishingHotbar4,
            CustomBinds.fishingHotbar5,
            CustomBinds.fishingHotbar6,
            CustomBinds.fishingHotbar7,
            CustomBinds.fishingHotbar8,
            CustomBinds.fishingHotbar9
        )

        for (i in 0..8) {
            val mapping = options.keyHotbarSlots[i]
            val customCode = customHotbars[i]
            val customKey = customCode.toInputKey()
            if (customKey != null) {
                originalKeys[mapping] = mapping.rfuKey
                mapping.setKey(customKey)
            }
        }

        val customLeft = CustomBinds.fishingLeftClick.toInputKey()
        if (customLeft != null) {
            originalKeys[options.keyAttack] = options.keyAttack.rfuKey
            options.keyAttack.setKey(customLeft)
        }

        val customRight = CustomBinds.fishingRightClick.toInputKey()
        if (customRight != null) {
            originalKeys[options.keyUse] = options.keyUse.rfuKey
            options.keyUse.setKey(customRight)
        }

        if (originalKeys.isNotEmpty()) {
            KeyMapping.resetMapping()
            isApplied = true
        }
    }

    @Synchronized
    fun revertKeybinds() {
        if (!isApplied) return

        for ((mapping, origKey) in originalKeys) {
            mapping.isDown = false
            mapping.setKey(origKey)
        }
        originalKeys.clear()
        KeyMapping.resetMapping()
        isApplied = false
    }

    fun onConfigChanged() {
        if (isApplied) {
            revertKeybinds()
            updateState()
        }
    }
}
