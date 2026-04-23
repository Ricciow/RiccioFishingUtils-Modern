package cloud.glitchdev.rfu.gui.components.seacreature

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.config.seacreatures.SeaCreatureSettingsManager
import cloud.glitchdev.rfu.constants.RareScDisplayDataType
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.constants.text.TextColor.GRAY
import cloud.glitchdev.rfu.constants.text.TextColor.WHITE
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.checkbox.UICheckbox
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.components.elementa.group.GroupMaxSizeConstraint
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import cloud.glitchdev.rfu.utils.dsl.toMcCodes
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*

class UISeaCreatureEditor : UIContainer() {
    private var currentSc: SeaCreatures? = null
    
    // Identity fields
    private lateinit var nameInput: UIDecoratedTextInput
    private lateinit var pluralInput: UIDecoratedTextInput
    private lateinit var articleInput: UIDecoratedTextInput
    private lateinit var styleInput: UIDecoratedTextInput
    
    // Settings checkboxes
    private lateinit var specialCheckbox: UICheckbox
    private lateinit var lsRangeCheckbox: UICheckbox
    private lateinit var bossbarCheckbox: UICheckbox
    private lateinit var gdragAlertCheckbox: UICheckbox
    private lateinit var rareSCAlertCheckbox: UICheckbox
    private lateinit var scDisplayColorInput: UIDecoratedTextInput

    // Preview texts
    private lateinit var previewNormal: UIText
    private lateinit var previewDouble: UIText
    private lateinit var previewDisplay: UIText

    init {
        create()
    }

    private fun create() {
        val scrollBarContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        } childOf this

        val scrollArea = ScrollComponent(verticalScrollEnabled = true).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent()
            height = 100.percent()
        } childOf scrollBarContainer

        val scrollbar = UIRoundedRectangle(2f).constrain {
            x = 0.pixels(true)
            y = 0.pixels()
            width = 3.pixels()
            height = 100.percent()
            color = UIScheme.secondaryColor.toConstraint()
        } childOf scrollBarContainer

        scrollArea.setScrollBarComponent(scrollbar, hideWhenUseless = true, isHorizontal = false)

        val content = UIRoundedRectangle(2f).constrain {
            x = CenterConstraint()
            y = 0.pixels()
            width = 100.percent()
            height = BoundingBoxConstraint() + 15.pixels
            color = UIScheme.contentBackground.toConstraint()
        } childOf scrollArea

        createIdentitySection(content)
        createSettingsSection(content)
        setupListeners()
    }

    private fun createIdentitySection(parent: UIComponent) {
        UIText("Identity").constrain {
            x = 10.pixels()
            y = 10.pixels()
            color = UIScheme.primaryTextColor.toConstraint()
        } childOf parent

        nameInput = addField("Name:", parent, 10f)
        pluralInput = addField("Plural:", parent)
        articleInput = addField("Article:", parent)
        styleInput = addField("Catch Color:", parent)

        // Preview Section
        previewNormal = UIText("Preview: ").constrain {
            x = 15.pixels()
            y = SiblingConstraint(10f)
        } childOf parent

        previewDouble = UIText("Preview: ").constrain {
            x = 15.pixels()
            y = SiblingConstraint(5f)
        } childOf parent

        scDisplayColorInput = addField("Display Color:", parent, 10f)
        
        previewDisplay = UIText("Preview: ").constrain {
            x = 15.pixels()
            y = SiblingConstraint(10f)
        } childOf parent
    }

    private fun createSettingsSection(parent: UIComponent) {
        // Settings Section
        UIText("Settings").constrain {
            x = 10.pixels()
            y = SiblingConstraint(15f)
            color = UIScheme.primaryTextColor.toConstraint()
        } childOf parent

        specialCheckbox = UICheckbox("Rare", false) { isRare ->
            lsRangeCheckbox.state = isRare
            bossbarCheckbox.state = isRare
            gdragAlertCheckbox.state = isRare
            rareSCAlertCheckbox.state = isRare
            refreshEnabledStates()
            saveCurrent() 
        }.constrain {
            x = 15.pixels()
            y = SiblingConstraint(10f)
            width = 100.pixels()
            height = 15.pixels()
        } childOf parent

        lsRangeCheckbox = UICheckbox("Lootshare Range", false) { saveCurrent() }.constrain {
            x = 15.pixels()
            y = SiblingConstraint(5f)
            width = 150.pixels()
            height = 15.pixels()
        } childOf parent

        bossbarCheckbox = UICheckbox("Bossbar", false) { saveCurrent() }.constrain {
            x = 15.pixels()
            y = SiblingConstraint(5f)
            width = 100.pixels()
            height = 15.pixels()
        } childOf parent

        gdragAlertCheckbox = UICheckbox("Gdrag Alert", false) { saveCurrent() }.constrain {
            x = 15.pixels()
            y = SiblingConstraint(5f)
            width = 100.pixels()
            height = 15.pixels()
        } childOf parent

        rareSCAlertCheckbox = UICheckbox("Rare Alert", false) { saveCurrent() }.constrain {
            x = 15.pixels()
            y = SiblingConstraint(5f)
            width = 100.pixels()
            height = 15.pixels()
        } childOf parent
    }

    private fun addField(label: String, parent: UIComponent, topPadding: Float = 5f): UIDecoratedTextInput {
        val container = UIContainer().constrain {
            x = 0.pixels()
            y = SiblingConstraint(topPadding)
            width = 100.percent()
            height = ChildBasedMaxSizeConstraint()
        } childOf parent

        val textContainer = UIContainer().constrain {
            x = 15.pixels()
            y = CenterConstraint()
            width = GroupMaxSizeConstraint("SCEditWindowField", ChildBasedMaxSizeConstraint())
            height = ChildBasedSizeConstraint()
        } childOf container

        UIText(label).constrain {
            x = 0.pixels
            y = CenterConstraint()
            width = ScaledTextConstraint(1f)
            height = TextAspectConstraint()
            color = UIScheme.secondaryTextColor.toConstraint()
        } childOf textContainer
        
        return UIDecoratedTextInput("", 2f).constrain {
            x = SiblingConstraint(2f)
            y = CenterConstraint()
            width = FillConstraint() - 32.pixels
            height = 16.pixels()
        } childOf container
    }

    private fun setupListeners() {
        // Add change listeners to inputs
        nameInput.onChange = { saveCurrent() }
        pluralInput.onChange = { saveCurrent() }
        articleInput.onChange = { saveCurrent() }
        styleInput.onChange = { saveCurrent() }
        scDisplayColorInput.onChange = { saveCurrent() }
    }

    fun loadSc(sc: SeaCreatures?) {
        if (sc == null) return
        val current = SeaCreatures.get(sc.scName) ?: sc
        currentSc = current
        
        // Load data
        nameInput.setText(current.getNameWithoutArticle())
        pluralInput.setText(current.getPluralName())
        articleInput.setText(current.getArticle())
        styleInput.setText(current.getStyleCode().replace("§", "&"))
        
        specialCheckbox.state = current.special
        lsRangeCheckbox.state = current.lsRangeEnabled
        bossbarCheckbox.state = current.bossbar
        gdragAlertCheckbox.state = current.gdragAlert
        rareSCAlertCheckbox.state = current.rareSCAlert
        scDisplayColorInput.setText(current.scDisplayColor.replace("§", "&"))
        
        refreshEnabledStates()
        updatePreviews()
    }

    private fun refreshEnabledStates() {
        val isRare = specialCheckbox.state
        lsRangeCheckbox.isEnabled = isRare
        bossbarCheckbox.isEnabled = isRare
        gdragAlertCheckbox.isEnabled = isRare
        rareSCAlertCheckbox.isEnabled = isRare
    }

    private fun updatePreviews() {
        val sc = currentSc ?: return
        val style = styleInput.getText()
        val name = nameInput.getText()
        val plural = pluralInput.getText()
        val article = articleInput.getText()
        val articleUpper = article.replaceFirstChar { it.uppercaseChar() }
        val mob = if (article.isNotEmpty()) "$article $name" else name
        val displayColor = scDisplayColorInput.getText().toMcCodes().ifEmpty { WHITE }

        val normalTemplate = SeaCreatureConfig.catchMessageTemplate
        val doubleHookTemplate = SeaCreatureConfig.doubleHookCatchMessageTemplate

        fun style(template: String): String {
            return template
                .replace("{article}", article)
                .replace("{article_upper}", articleUpper)
                .replace("{name}", name)
                .replace("{style}", style)
                .replace("{plural}", plural)
                .replace("{mob}", mob)
                .replace("{mobs}", plural)
                .toMcCodes()
        }

        previewNormal.setText("Preview: ${style(normalTemplate)}")
        previewDouble.setText("Preview: ${style(doubleHookTemplate)}")

        val dataOrder = SeaCreatureConfig.rareScDisplayDataOrder
        val displayPreviewLine = buildString {
            append("$displayColor${BOLD}$name:")
            dataOrder.forEach { dataType ->
                when (dataType) {
                    RareScDisplayDataType.STREAK -> append(" ${YELLOW}5")
                    RareScDisplayDataType.AVERAGE -> append(" ${GRAY}(${YELLOW}20$GRAY)")
                    RareScDisplayDataType.TOTAL -> append(" $displayColor[${YELLOW}10$displayColor]")
                    RareScDisplayDataType.TIME_SINCE -> append(" ${WHITE}10s")
                }
            }
        }
        previewDisplay.setText("Preview: $displayPreviewLine")
    }

    private fun saveCurrent() {
        val sc = currentSc ?: return
        
        val changed = SeaCreatureSettingsManager.updateCreature(sc.scName) { current ->
            current.copy(
                name = nameInput.getText(),
                plural = pluralInput.getText(),
                article = articleInput.getText(),
                style = styleInput.getText().toMcCodes(),
                special = specialCheckbox.state,
                lsRangeEnabled = lsRangeCheckbox.state,
                bossbar = bossbarCheckbox.state,
                gdragAlert = gdragAlertCheckbox.state,
                rareSCAlert = rareSCAlertCheckbox.state,
                scDisplayColor = scDisplayColorInput.getText().toMcCodes()
            )
        }
        
        updatePreviews()
        if (changed) {
            SeaCreatureSettingsManager.save()
        }
    }
}


