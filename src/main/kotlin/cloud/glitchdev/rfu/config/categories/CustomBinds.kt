package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.feature.fishing.FishingKeybindsHandler
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.KeyBuilder

object CustomBinds : ObjectKt() {
    private fun keybind(value: Int, builder: KeyBuilder.() -> Unit = {}) = ObservableEntry(key(value, builder)) { _, _ -> FishingKeybindsHandler.onConfigChanged() }

    var resetBindsKeybind by keybind(0) {
        name = Literal("Panic Reset")
        description = Literal("Keybind to immediately disable all overrides.")
    }

    var fishingHotbar1 by keybind(49) {
        name = Literal("Hotbar 1")
        description = Literal("Keybind for hotbar slot 1 when fishing.")
    }

    var fishingHotbar2 by keybind(50) {
        name = Literal("Hotbar 2")
        description = Literal("Keybind for hotbar slot 2 when fishing.")
    }

    var fishingHotbar3 by keybind(51) {
        name = Literal("Hotbar 3")
        description = Literal("Keybind for hotbar slot 3 when fishing.")
    }

    var fishingHotbar4 by keybind(52) {
        name = Literal("Hotbar 4")
        description = Literal("Keybind for hotbar slot 4 when fishing.")
    }

    var fishingHotbar5 by keybind(53) {
        name = Literal("Hotbar 5")
        description = Literal("Keybind for hotbar slot 5 when fishing.")
    }

    var fishingHotbar6 by keybind(54) {
        name = Literal("Hotbar 6")
        description = Literal("Keybind for hotbar slot 6 when fishing.")
    }

    var fishingHotbar7 by keybind(55) {
        name = Literal("Hotbar 7")
        description = Literal("Keybind for hotbar slot 7 when fishing.")
    }

    var fishingHotbar8 by keybind(56) {
        name = Literal("Hotbar 8")
        description = Literal("Keybind for hotbar slot 8 when fishing.")
    }

    var fishingHotbar9 by keybind(57) {
        name = Literal("Hotbar 9")
        description = Literal("Keybind for hotbar slot 9 when fishing.")
    }

    var fishingLeftClick by keybind(-100) {
        name = Literal("Left Click")
        description = Literal("Keybind for left click (attack) when fishing.")
    }

    var fishingRightClick by keybind(-101) {
        name = Literal("Right Click")
        description = Literal("Keybind for right click (use item) when fishing.")
    }
}
