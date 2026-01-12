package cloud.glitchdev.rfu.gui.components.dropdown

import cloud.glitchdev.rfu.model.data.DataOption

class UISelectionDropdown(
    values: ArrayList<DataOption>,
    val selectionLimit: Int = Int.MAX_VALUE, // Default to no limit
    preSelectedIndices: Set<Int> = emptySet(),
    radiusProps: Float,
    hideArrow: Boolean = false,
    label: String = "",
    var onSelectionChanged: (List<DataOption>) -> Unit = {}
) : UIAbstractDropdown(values, radiusProps, hideArrow, label) {

    private val selectedIndices = HashSet<Int>()

    init {
        selectedIndices.addAll(preSelectedIndices)
        updateDropdownState()
    }

    fun runListener() {
        val selectedValues = values.filterIndexed { i, _ -> selectedIndices.contains(i) }
        onSelectionChanged(selectedValues)
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

        runListener()

        refreshOptionColors()

        if (label.isEmpty()) {
            text.setText(getDropdownDisplayText())
        }
    }

    override fun shouldHover() : Boolean {
        val size = selectedIndices.size
        return size < selectionLimit
    }

    override fun isOptionDisabled(index: Int): Boolean {
        return !isOptionSelected(index) && !shouldHover()
    }

    override fun isOptionSelected(index: Int): Boolean {
        return selectedIndices.contains(index)
    }

    override fun getDropdownDisplayText(): String {
        if (selectedIndices.isEmpty()) return "None"
        if (selectedIndices.size == 1) return values[selectedIndices.first()].label
        if (selectedIndices.size == values.size) return "All"

        return "${selectedIndices.size} Selected"
    }

    fun setOptionsStates(options: List<DataOption>, state : Boolean) {
        for(option in options) {
            setOptionState(option, state)
        }
    }

    fun setOptionState(option : DataOption, state : Boolean) {
        val index = values.indexOf(option)
        if(index != -1) {
            if(!state) {
                selectedIndices.remove(index)
            } else {
                selectedIndices.add(index)
            }
        }
    }

    override fun setValues(newValues: List<DataOption>) {
        val stillSelected = hashSetOf<DataOption>()
        for(index in selectedIndices) {
            val value = values[index]
            if(newValues.contains(value)) {
                stillSelected.add(value)
            }
        }
        selectedIndices.clear()
        super.setValues(newValues)

        for(value in stillSelected) {
            selectedIndices.add(values.indexOf(value))
        }

        runListener()
        updateDropdownState()
    }

    fun getSelectedItems(): List<DataOption> {
        return values.filterIndexed { index, _ -> selectedIndices.contains(index) }
    }
}