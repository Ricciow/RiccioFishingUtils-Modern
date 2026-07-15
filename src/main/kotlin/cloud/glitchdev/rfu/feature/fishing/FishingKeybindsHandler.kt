package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.CustomBinds
import cloud.glitchdev.rfu.data.mob.MobManager
import cloud.glitchdev.rfu.events.managers.KeyboardEvents.registerKeyboardEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.isFishingRod
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.mixin.KeyMappingAccessor
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping

@RFUFeature
object FishingKeybindsHandler : Feature {
    private var isRedirecting = false
    var lastCastTime = 0L

    override fun onInitialize() {
        registerKeyboardEvent({ CustomBinds.resetBindsKeybind }, onPress = {
            if (GeneralFishing.overrideFishingKeybinds) {
                GeneralFishing.overrideFishingKeybinds = false
                Chat.sendMessage(TextUtils.rfuLiteral("Custom fishing keybinds disabled.", TextColor.LIGHT_GREEN))
            }
        })
    }

    val KeyMapping.rfuKey: InputConstants.Key
        get() = (this as KeyMappingAccessor).`rfu$GetKey`()

    fun isOverriding(): Boolean {
        if (!GeneralFishing.overrideFishingKeybinds) return false
        mc.player ?: return false
        
        //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
        val screen = mc.gui.screen()
        //~}
        if (screen != null) return false

        if (GeneralFishing.disableOnJawbus && MobManager.getEntities().any { it.sbName == "Lord Jawbus" }) {
            return false
        }

        if (FishingSession.isFishing && (System.currentTimeMillis() - lastCastTime < 30_000)) {
            return true
        }

        return false
    }


    fun handleKeySet(key: InputConstants.Key, state: Boolean): Boolean {
        if (isRedirecting) return false
        val options = mc.options
        if (state && key == options.keyUse.rfuKey) {
            val player = mc.player
            if (player != null && player.mainHandItem.isFishingRod()) {
                lastCastTime = System.currentTimeMillis()
            }
        }

        if (!isOverriding()) return false

        val targetKey = getRedirectedTargetKey(key)
        if (targetKey != null) {
            isRedirecting = true
            try {
                KeyMapping.set(targetKey, state)
            } finally {
                isRedirecting = false
            }
            return true
        }

        if (isStandardOverriddenKey(key)) {
            return true
        }

        return false
    }

    fun handleKeyClick(key: InputConstants.Key): Boolean {
        if (isRedirecting) return false
        if (!isOverriding()) return false

        val targetKey = getRedirectedTargetKey(key)
        if (targetKey != null) {
            isRedirecting = true
            try {
                KeyMapping.click(targetKey)
            } finally {
                isRedirecting = false
            }
            return true
        }
        if (isStandardOverriddenKey(key)) {
            return true
        }

        return false
    }

    private fun getRedirectedTargetKey(physicalKey: InputConstants.Key): InputConstants.Key? {
        val options = mc.options
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
            val customKey = customHotbars[i]
            if (customKey != 0 && matchesKeybind(physicalKey, customKey)) {
                return options.keyHotbarSlots[i].rfuKey
            }
        }

        val customLeft = CustomBinds.fishingLeftClick
        if (customLeft != 0 && matchesKeybind(physicalKey, customLeft)) {
            return options.keyAttack.rfuKey
        }

        val customRight = CustomBinds.fishingRightClick
        if (customRight != 0 && matchesKeybind(physicalKey, customRight)) {
            return options.keyUse.rfuKey
        }

        return null
    }

    private fun matchesKeybind(physicalKey: InputConstants.Key, configValue: Int): Boolean {
        return if (configValue < 0) {
            physicalKey.type == InputConstants.Type.MOUSE && physicalKey.value == (-configValue - 100)
        } else {
            physicalKey.type == InputConstants.Type.KEYSYM && physicalKey.value == configValue
        }
    }

    private fun isStandardOverriddenKey(key: InputConstants.Key): Boolean {
        val options = mc.options ?: return false

        for (i in 0..8) {
            if (options.keyHotbarSlots[i].rfuKey == key) {
                return true
            }
        }

        return options.keyAttack.rfuKey == key || options.keyUse.rfuKey == key
    }
}

