package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.utils.dsl.rfuKey
import com.mojang.blaze3d.platform.InputConstants
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.KeyBuilder

object CustomBinds : ObjectKt() {
    fun rebuildKey(value : Int, builder: KeyBuilder.() -> Unit = {}) = ObservableEntry(key(value, builder)) { _, _ -> rebuildCache() }

    var resetBindsKeybind by rebuildKey(0) {
        name = Literal("Panic Reset")
        description = Literal("Keybind to immediately disable all overrides.")
    }

    var fishingHotbar1 by rebuildKey(49) {
        name = Literal("Hotbar 1")
        description = Literal("Keybind for hotbar slot 1 when fishing.")
    }

    var fishingHotbar2 by rebuildKey(50) {
        name = Literal("Hotbar 2")
        description = Literal("Keybind for hotbar slot 2 when fishing.")
    }

    var fishingHotbar3 by rebuildKey(51) {
        name = Literal("Hotbar 3")
        description = Literal("Keybind for hotbar slot 3 when fishing.")
    }

    var fishingHotbar4 by rebuildKey(52) {
        name = Literal("Hotbar 4")
        description = Literal("Keybind for hotbar slot 4 when fishing.")
    }

    var fishingHotbar5 by rebuildKey(53) {
        name = Literal("Hotbar 5")
        description = Literal("Keybind for hotbar slot 5 when fishing.")
    }

    var fishingHotbar6 by rebuildKey(54) {
        name = Literal("Hotbar 6")
        description = Literal("Keybind for hotbar slot 6 when fishing.")
    }

    var fishingHotbar7 by rebuildKey(55) {
        name = Literal("Hotbar 7")
        description = Literal("Keybind for hotbar slot 7 when fishing.")
    }

    var fishingHotbar8 by rebuildKey(56) {
        name = Literal("Hotbar 8")
        description = Literal("Keybind for hotbar slot 8 when fishing.")
    }

    var fishingHotbar9 by rebuildKey(57) {
        name = Literal("Hotbar 9")
        description = Literal("Keybind for hotbar slot 9 when fishing.")
    }

    var fishingLeftClick by rebuildKey(-100) {
        name = Literal("Left Click")
        description = Literal("Keybind for left click (attack) when fishing.")
    }

    var fishingRightClick by rebuildKey(-101) {
        name = Literal("Right Click")
        description = Literal("Keybind for right click (use item) when fishing.")
    }

    val redirectMap = HashMap<InputConstants.Key, InputConstants.Key>()
    val standardKeys = HashSet<InputConstants.Key>()

    private fun createKey(configValue: Int): InputConstants.Key {
        return if (configValue < 0) {
            InputConstants.Type.MOUSE.getOrCreate(-configValue - 100)
        } else {
            InputConstants.Type.KEYSYM.getOrCreate(configValue)
        }
    }

    fun rebuildCache() {
        redirectMap.clear()
        standardKeys.clear()
        val options = mc.options

        val customHotbars = arrayOf(
            fishingHotbar1,
            fishingHotbar2,
            fishingHotbar3,
            fishingHotbar4,
            fishingHotbar5,
            fishingHotbar6,
            fishingHotbar7,
            fishingHotbar8,
            fishingHotbar9
        )

        for (i in 0..8) {
            val customKeyVal = customHotbars[i]
            if (customKeyVal != 0) {
                redirectMap[createKey(customKeyVal)] = options.keyHotbarSlots[i].rfuKey
            }
            standardKeys.add(options.keyHotbarSlots[i].rfuKey)
        }

        val customLeft = fishingLeftClick
        if (customLeft != 0) {
            redirectMap[createKey(customLeft)] = options.keyAttack.rfuKey
        }
        standardKeys.add(options.keyAttack.rfuKey)

        val customRight = fishingRightClick
        if (customRight != 0) {
            redirectMap[createKey(customRight)] = options.keyUse.rfuKey
        }
        standardKeys.add(options.keyUse.rfuKey)
    }
}
