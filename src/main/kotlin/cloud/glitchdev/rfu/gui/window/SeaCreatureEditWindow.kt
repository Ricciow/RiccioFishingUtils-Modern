package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.seacreature.UISeaCreatureEditor
import cloud.glitchdev.rfu.gui.components.seacreature.UISeaCreatureList
import cloud.glitchdev.rfu.utils.gui.addHoverColoring
import com.teamresourceful.resourcefulconfig.client.ConfigScreen
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*

@Suppress("UnstableApiUsage")
class SeaCreatureEditWindow(val settingsScreen : ConfigScreen?) : BaseWindow(true) {
    private val primaryColor = UIScheme.windowBackground.toConstraint()
    private val radius = 5f
    private val spacing = 5f
    private val sidebarWidth = 150.pixels()
    private val headerHeight = 25.pixels()

    private lateinit var mainContainer: UIRoundedRectangle
    private lateinit var sidebar: UISeaCreatureList
    private lateinit var editor: UISeaCreatureEditor
    private lateinit var contentArea: UIContainer
    
    private var currentSc: SeaCreatures? = null

    init {
        create()

        SeaCreatures.entries.firstOrNull()?.let { loadSc(it) }
    }

    private fun create() {
        mainContainer = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent()
            height = 100.percent()
            color = primaryColor
        } childOf window

        createSidebar()
        createContentArea()
    }

    private fun createSidebar() {
        sidebar = UISeaCreatureList { sc ->
            loadSc(sc)
        }.constrain {
            x = spacing.pixels()
            y = spacing.pixels()
            width = sidebarWidth
            height = 100.percent() - (spacing * 2).pixels()
        } childOf mainContainer
    }

    private fun createContentArea() {
        contentArea = UIContainer().constrain {
            x = SiblingConstraint(spacing)
            y = spacing.pixels()
            width = 100.percent() - sidebarWidth - (spacing * 3).pixels()
            height = 100.percent() - (spacing * 2).pixels()
        } childOf mainContainer

        createHeader(contentArea)
        createEditorArea(contentArea)
    }

    private fun createHeader(parent: UIComponent) {
        val header = UIContainer().constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = headerHeight
        } childOf parent

        UIText("Edit Sea Creatures").constrain {
            x = 5.pixels()
            y = CenterConstraint()
            color = UIScheme.primaryTextColor.toConstraint()
        } childOf header
    }

    private fun createEditorArea(parent: UIComponent) {
        editor = UISeaCreatureEditor().constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 100.percent()
            height = FillConstraint()
        } childOf parent
    }

    private fun loadSc(sc: SeaCreatures) {
        currentSc = sc
        sidebar.setSelected(sc)
        editor.loadSc(sc)
    }

    override fun onWindowClose() {
        displayScreen(settingsScreen)
    }
}
