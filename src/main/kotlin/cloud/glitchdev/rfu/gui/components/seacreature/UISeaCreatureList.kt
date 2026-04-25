package cloud.glitchdev.rfu.gui.components.seacreature

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*

class UISeaCreatureList(val onSelect: (SeaCreatures) -> Unit) : UIRoundedRectangle(2f) {
    private lateinit var searchBar: UIDecoratedTextInput
    private lateinit var scrollArea: ScrollComponent
    private val listButtons = mutableMapOf<String, UIComponent>()
    private var currentSc: SeaCreatures? = null

    init {
        create()
    }

    private fun create() {
        constrain {
            color = UIScheme.sidebarBackground.toConstraint()
        }

        searchBar = UIDecoratedTextInput("Search...", 2f).constrain {
            x = 0.pixels
            y = 5.pixels()
            width = 100.percent() - 5.pixels
            height = 18.pixels()
        } childOf this
        
        searchBar.onChange = { query ->
            refreshList(query)
        }

        scrollArea = ScrollComponent(verticalScrollEnabled = true).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 100.percent()
            height = 100.percent() - 28.pixels()
        } childOf this

        val scrollbar = UIRoundedRectangle(2f).constrain {
            x = 0.pixels(true)
            y = 0.pixels()
            width = 3.pixels()
            height = 100.percent()
            color = UIScheme.secondaryColor.toConstraint()
        } childOf this

        scrollArea.setScrollBarComponent(scrollbar, hideWhenUseless = true, isHorizontal = false)

        refreshList("")
    }

    fun refreshList(query: String) {
        scrollArea.clearChildren()
        listButtons.clear()

        val grouped = SeaCreatures.entries
            .filter { sc ->
                sc.scName.contains(query, ignoreCase = true) ||
                sc.scDisplayName.contains(query, ignoreCase = true) ||
                sc.category.displayName.contains(query, ignoreCase = true)
            }
            .groupBy { it.category }
            .toSortedMap(compareBy { it.displayName })

        grouped.forEach { (category, creatures) ->
            val headerContainer = UIContainer().constrain {
                x = 0.pixels()
                y = SiblingConstraint()
                width = 100.percent()
                height = 15.pixels()
            } childOf scrollArea

            UIText(category.displayName).constrain {
                x = 3.pixels()
                y = CenterConstraint()
                color = UIScheme.secondaryTextColor.toConstraint()
                textScale = 0.8.pixels()
            } childOf headerContainer

            creatures.sortedBy { it.scName }.forEach { sc ->
                val container = UIContainer().constrain {
                    x = 0.pixels()
                    y = SiblingConstraint()
                    width = 100.percent()
                    height = 20.pixels()
                }.onMouseClick {
                    onSelect(sc)
                } childOf scrollArea

                val text = UIText(sc.scName).constrain {
                    x = 10.pixels()
                    y = CenterConstraint()
                    color = (if (sc == currentSc) UIScheme.selectedTextColor else UIScheme.primaryTextColor).toConstraint()
                } childOf container

                container.onMouseEnter {
                    text.animate {
                        setColorAnimation(Animations.OUT_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.secondaryColor.toConstraint())
                    }
                }.onMouseLeave {
                    text.animate {
                        setColorAnimation(Animations.OUT_EXP, UIScheme.HOVER_EFFECT_DURATION, (if (sc == currentSc) UIScheme.selectedTextColor else UIScheme.primaryTextColor).toConstraint())
                    }
                }

                listButtons[sc.scName] = text
            }
        }
    }

    fun setSelected(sc: SeaCreatures?) {
        currentSc?.let { oldSc ->
            listButtons[oldSc.scName]?.let { (it as UIText).constrain { color = UIScheme.primaryTextColor.toConstraint() } }
        }
        currentSc = sc
        sc?.let { newSc ->
            listButtons[newSc.scName]?.let { (it as UIText).constrain { color = UIScheme.selectedTextColor.toConstraint() } }
        }
    }
}
