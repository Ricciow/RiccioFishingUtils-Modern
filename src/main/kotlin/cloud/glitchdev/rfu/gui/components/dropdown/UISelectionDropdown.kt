package cloud.glitchdev.rfu.gui.components.dropdown

import cloud.glitchdev.rfu.model.data.DataOption

class UISelectionDropdown(
    values: ArrayList<DataOption>,
    val selectionLimit: Int = Int.MAX_VALUE, // Default to no limit
    preSelectedIndices: Set<Int> = emptySet(),
    radiusProps: Float,
    hideArrow: Boolean = false,
    label: String = "",
    val onSelectionChanged: (List<Any>) -> Unit = {}
) : UIAbstractDropdown(values, radiusProps, hideArrow, label) {

    private val selectedIndices = HashSet<Int>()

    init {
        selectedIndices.addAll(preSelectedIndices)
        updateDropdownState()
    }

    override fun onOptionClicked(option: DataOption, index: Int) {
        if (selectedIndices.contains(index)) {
            selectedIndices.remove(index)
        } else {
            if (selectedIndices.size < selectionLimit) {
                selectedIndices.add(index)
            } else {
                return
            }
        }

        val selectedValues = values.filterIndexed { i, _ -> selectedIndices.contains(i) }.map { it.value }
        onSelectionChanged(selectedValues)

        refreshOptionColors()

        if (label.isEmpty()) {
            text.setText(getDropdownDisplayText())
        }
    }

    override fun shouldHover() : Boolean {
        return selectedIndices.size < selectionLimit
    }

    override fun isOptionSelected(index: Int): Boolean {
        return selectedIndices?.contains(index) ?: false
    }

    override fun getDropdownDisplayText(): String {
        if (selectedIndices.isEmpty()) return "None"
        if (selectedIndices.size == 1) return values[selectedIndices.first()].label
        if (selectedIndices.size == values.size) return "All"

        return "${selectedIndices.size} Selected"
    }

    fun getSelectedItems(): List<DataOption> {
        return values.filterIndexed { index, _ -> selectedIndices.contains(index) }
    }
}