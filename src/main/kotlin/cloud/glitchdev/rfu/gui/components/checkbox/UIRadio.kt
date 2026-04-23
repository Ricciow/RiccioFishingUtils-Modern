package cloud.glitchdev.rfu.gui.components.checkbox

import cloud.glitchdev.rfu.gui.UIScheme
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
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.components.Colorable
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

class UIRadio(
    val values: ArrayList<DataOption>,
    selectedValue: Int,
    var onChange: (DataOption) -> Unit = {}
) : UIContainer(), Colorable {
    var primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    var hoverColor = UIScheme.secondaryColor.toConstraint()
    var textColor = UIScheme.primaryTextColor.toConstraint()

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

    fun setSelected(option: DataOption) {
        selectedValue = values.indexOf(option)
    }

    fun create() {
        for((index, option) in values.withIndex()) {
            val checkbox = UICheckbox(option.label, index == selectedValue, false).colors {
                this.primaryColor = this@UIRadio.primaryColor
                this.hoverColor = this@UIRadio.hoverColor
                this.textColor = this@UIRadio.textColor
            }
            
            checkbox.onChange = {
                selectedValue = index
                onChange(getSelectedValue())
            }
            
            checkbox.constrain {
                x = CramSiblingConstraint(2f)
                y = CramSiblingConstraint(2f) + if(index == 0) CenterConstraint() else 0.pixels()
                width = ChildBasedSizeConstraint()
                height = 100.percent()
            } childOf this

            checkboxes.add(checkbox)
        }
    }

    fun getSelectedValue() : DataOption {
        return values.getOrNull(selectedValue) ?: DataOption("None", "None")
    }

    override fun refreshColors() {
        checkboxes.forEach {
            it.colors {
                this.primaryColor = this@UIRadio.primaryColor
                this.hoverColor = this@UIRadio.hoverColor
                this.textColor = this@UIRadio.textColor
            }
        }
    }
}