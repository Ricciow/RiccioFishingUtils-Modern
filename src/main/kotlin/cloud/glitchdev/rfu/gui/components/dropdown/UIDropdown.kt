package cloud.glitchdev.rfu.gui.components.dropdown

import cloud.glitchdev.rfu.model.data.DataOption

class UIDropdown(
    values: ArrayList<DataOption>,
    var selectedIndex: Int = 0,
    radiusProps: Float,
    hideArrow: Boolean = false,
    label: String = "",
    var onSelect: (DataOption) -> Unit = {}
) : UIAbstractDropdown(values, radiusProps, hideArrow, label) {

    fun setSelected(option: DataOption) {
        selectedIndex = values.indexOf(option)
        updateDropdownState()
    }

    override fun onOptionClicked(option: DataOption, index: Int) {
        selectedIndex = index
        onSelect(getSelectedItem())
        isOpen = false
        updateDropdownState()
    }

    override fun isOptionSelected(index: Int): Boolean {
        return index == selectedIndex
    }

    override fun getDropdownDisplayText(): String {
        return values.getOrNull(selectedIndex)?.label ?: "Dropdown"
    }

    fun getSelectedItem(): DataOption {
        return values.getOrNull(selectedIndex) ?: DataOption("None", "None")
    }
}