package cloud.glitchdev.rfu.gui.components.dropdown

import cloud.glitchdev.rfu.model.data.DataOption

class UIDropdown(
    values: ArrayList<DataOption>,
    var selectedIndex: Int = 0,
    radiusProps: Float,
    hideArrow: Boolean = false,
    label: String = "",
    val onSelect: (Any) -> Unit = {}
) : UIAbstractDropdown(values, radiusProps, hideArrow, label) {

    override fun onOptionClicked(option: DataOption, index: Int) {
        selectedIndex = index
        onSelect(option.value)
        isOpen = false
        updateDropdownState()
    }

    override fun isOptionSelected(index: Int): Boolean {
        return index == selectedIndex
    }

    override fun getDropdownDisplayText(): String {
        return values.getOrNull(selectedIndex)?.label ?: "Dropdown"
    }
}