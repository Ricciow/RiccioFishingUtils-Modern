package cloud.glitchdev.rfu.gui.components.checkbox

import cloud.glitchdev.rfu.model.data.DataOption
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus

class UIRadio(val values : ArrayList<DataOption>, selectedValue : Int, val callback: (Any) -> Unit = {}) : UIContainer() {
    val checkboxes : ArrayList<UICheckbox> = arrayListOf()

    private var selectedValue : Int = selectedValue
        set(value) {
            for((index, checkbox) in checkboxes.withIndex()) {
                checkbox.state = index == value
            }
            field = value
        }

    init {
        create()
    }

    fun create() {
        for((index, option) in values.withIndex()) {
            println("$index, $selectedValue, ${index == selectedValue}")
            val checkbox = UICheckbox(option.label, index == selectedValue, false) { value ->
                selectedValue = index
                callback(value)
            }.constrain {
                x = CramSiblingConstraint(2f)
                y = CramSiblingConstraint(2f) + if(index == 0) CenterConstraint() else 0.pixels()
                width = ChildBasedSizeConstraint()
                height = 100.percent()
            } childOf this

            checkboxes.add(checkbox)
        }
    }
}