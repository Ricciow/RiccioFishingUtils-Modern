package cloud.glitchdev.rfu

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UKeyboard
import cloud.glitchdev.rfu.RicciosFinestUtilities.Companion.minecraft
import java.awt.Color

class TelaTeste : WindowScreen(ElementaVersion.V10) {

    init {
        window.onKeyType { char, id ->
            if(id == UKeyboard.KEY_ESCAPE) {
                minecraft.send {
                    displayScreen(null)
                }
            }
        }

        val box = UIBlock(Color.DARK_GRAY).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 200.pixels()
            height = 100.pixels()
        } childOf window

        // Example: Add text inside the box
        UIText("Hello Elementa!").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.5f.pixels() // Sets text size
        } childOf box
    }
}