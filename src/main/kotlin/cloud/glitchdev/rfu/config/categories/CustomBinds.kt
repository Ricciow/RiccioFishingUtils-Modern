package cloud.glitchdev.rfu.config.categories

import com.teamresourceful.resourcefulconfigkt.api.ObjectKt

object CustomBinds : ObjectKt() {
    var resetBindsKeybind by key(0) {
        name = Literal("Panic Reset")
        description = Literal("Keybind to immediately disable all overrides.")
    }

    var fishingHotbar1 by key(49) {
        name = Literal("Hotbar 1")
        description = Literal("Keybind for hotbar slot 1 when fishing.")
    }


    var fishingHotbar2 by key(50) {
        name = Literal("Hotbar 2")
        description = Literal("Keybind for hotbar slot 2 when fishing.")
    }

    var fishingHotbar3 by key(51) {
        name = Literal("Hotbar 3")
        description = Literal("Keybind for hotbar slot 3 when fishing.")
    }

    var fishingHotbar4 by key(52) {
        name = Literal("Hotbar 4")
        description = Literal("Keybind for hotbar slot 4 when fishing.")
    }

    var fishingHotbar5 by key(53) {
        name = Literal("Hotbar 5")
        description = Literal("Keybind for hotbar slot 5 when fishing.")
    }

    var fishingHotbar6 by key(54) {
        name = Literal("Hotbar 6")
        description = Literal("Keybind for hotbar slot 6 when fishing.")
    }

    var fishingHotbar7 by key(55) {
        name = Literal("Hotbar 7")
        description = Literal("Keybind for hotbar slot 7 when fishing.")
    }

    var fishingHotbar8 by key(56) {
        name = Literal("Hotbar 8")
        description = Literal("Keybind for hotbar slot 8 when fishing.")
    }

    var fishingHotbar9 by key(57) {
        name = Literal("Hotbar 9")
        description = Literal("Keybind for hotbar slot 9 when fishing.")
    }

    var fishingLeftClick by key(-100) {
        name = Literal("Left Click")
        description = Literal("Keybind for left click (attack) when fishing.")
    }

    var fishingRightClick by key(-101) {
        name = Literal("Right Click")
        description = Literal("Keybind for right click (use item) when fishing.")
    }
}
