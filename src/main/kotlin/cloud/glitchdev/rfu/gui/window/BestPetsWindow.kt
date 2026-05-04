package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.components.elementa.JustifiedCramSiblingConstraint
import cloud.glitchdev.rfu.gui.components.elementa.group.GroupManager
import cloud.glitchdev.rfu.gui.components.pets.UIPetCard
import cloud.glitchdev.rfu.gui.components.pets.UIPetFilterArea
import cloud.glitchdev.rfu.model.pets.PetAuctionResponse
import cloud.glitchdev.rfu.utils.network.api.PetsApi
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect

object BestPetsWindow : BaseWindow(false) {
    private val primaryColor = UIScheme.pfWindowBackground.toConstraint()
    private val headerHeight = 30.pixels
    private val spacing = UIScheme.pfSpacing
    private val smallSpacing = UIScheme.pfSmallSpacing
    private var filtersOpen = false
    private var reloading = false
        set(value) {
            field = value
            refreshButton.disabled = reloading
        }

    private lateinit var filterButton: UIButton
    private lateinit var refreshButton: UIButton
    private lateinit var filterArea: UIContainer
    private lateinit var filterContainer: UIPetFilterArea
    private lateinit var scrollArea: ScrollComponent
    private var pets: List<PetAuctionResponse> = emptyList()

    init {
        create()
    }

    private fun create() {
        val radius = 5f

        val background = UIRoundedRectangle(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 80.percent
            height = 80.percent
            color = primaryColor
        } childOf window

        val useableArea = UIContainer().constrain {
            x = CenterConstraint()
            y = (radius / 2).pixels
            width = 100.percent
            height = 100.percent - radius.pixels
        } childOf background

        createHeader(useableArea)
        createFilterArea(useableArea)
        UIBlock().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent - spacing.pixels
            height = 1.pixels
            color = UIScheme.pfWindowSeparator.toConstraint()
        } childOf useableArea

        val contentWrapper = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent - (2 * spacing).pixels
            height = FillConstraint()
        } childOf useableArea effect ScissorEffect()

        scrollArea = ScrollComponent().constrain {
            x = 0.pixels
            y = smallSpacing.pixels
            width = FillConstraint() - smallSpacing.pixels
            height = 100.percent - smallSpacing.pixels
        } childOf contentWrapper

        val scrollBar = UIRoundedRectangle(5f).constrain {
            x = 0.pixels(true)
            width = 3.pixels
            color = UIScheme.pfScrollBar.toConstraint()
        } childOf contentWrapper

        scrollArea.setScrollBarComponent(scrollBar, hideWhenUseless = true, isHorizontal = false)
    }

    private fun createHeader(background: UIComponent) {
        val header = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent
            height = headerHeight
        } childOf background

        UIText("Best Pets to Level").constrain {
            x = spacing.pixels
            y = CenterConstraint()
            width = ScaledTextConstraint(1.5f)
            height = TextAspectConstraint()
            color = UIScheme.pfTitleText.toConstraint()
        } childOf header

        val rightArea = UIContainer().constrain {
            x = spacing.pixels(true)
            y = CenterConstraint()
            width = 30.percent
            height = 100.percent - 5.pixels
        } childOf header

        val filterImage = UIImage.ofResource("/assets/rfu/ui/filter.png")
        filterButton = UIButton.withImage(filterImage, 5f) {
            filtersOpen = !filtersOpen
            onUpdate()
        }.constrain {
            x = SiblingConstraint(2f, alignOpposite = true)
            y = CenterConstraint()
            height = 100.percent
            width = AspectConstraint(1f)
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf rightArea

        val refreshImage = UIImage.ofResource("/assets/rfu/ui/refresh.png")
        refreshButton = UIButton.withImage(refreshImage, 5f) {
            fetchData()
        }.constrain {
            x = SiblingConstraint(2f, alignOpposite = true)
            y = CenterConstraint()
            height = 100.percent
            width = AspectConstraint(1f)
        }.colors {
            primaryColor = UIScheme.pfInputBg.toConstraint()
            hoverColor = UIScheme.pfInputBgHovered.toConstraint()
        } childOf rightArea
    }

    private fun createFilterArea(background: UIComponent) {
        filterArea = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent
            height = 0.pixels
        } childOf background effect ScissorEffect()

        filterContainer = UIPetFilterArea {
            fetchData()
        }.constrain {
            x = CenterConstraint()
            y = 0.pixels
            width = 100.percent
            height = ChildBasedSizeConstraint() + smallSpacing.pixels
        } childOf filterArea

        fetchData()
    }

    private fun fetchData() {
        if(!::filterContainer.isInitialized) return
        reloading = true
        PetsApi.getBestPets(
            category = filterContainer.getCategory(),
            rarity = filterContainer.getRarity(),
            count = filterContainer.getCount(),
            filterCandy = filterContainer.getFilterCandy(),
            unique = filterContainer.getUnique(),
            maxLevel = filterContainer.getMaxLevel()
        ) { fetchedPets ->
            pets = fetchedPets
            reloading = false
            mc.execute { updatePetsList() }
        }
    }

    private fun updatePetsList() {
        if (!::scrollArea.isInitialized) return
        scrollArea.clearChildren()
        GroupManager.clearGroup("PetCardMain")
        GroupManager.clearGroup("PetCardInnerBg")
        GroupManager.clearGroup("PetCardInner")
        GroupManager.clearGroup("PetCardRight")
        
        pets.forEach { pet ->
            UIPetCard(pet, 5f).constrain {
                x = JustifiedCramSiblingConstraint(2f)
                y = JustifiedCramSiblingConstraint(2f)
                width = 33.percent
            } childOf scrollArea
        }
    }

    private fun onUpdate() {
        if (filtersOpen) {
            filterButton.colors {
                textColor = UIScheme.pfFilterButtonSelected.toConstraint()
                hoverTextColor = UIScheme.pfFilterButtonSelected.toConstraint()
                primaryColor = UIScheme.pfFilterButtonSelectedBg.toConstraint()
                hoverColor = UIScheme.pfInputBgHovered.toConstraint()
            }
            filterArea.animate {
                setHeightAnimation(Animations.OUT_EXP, 0.5f, ChildBasedSizeConstraint())
            }
        } else {
            filterButton.colors {
                textColor = UIScheme.primaryTextColor.toConstraint()
                hoverTextColor = UIScheme.primaryTextColor.toConstraint()
                primaryColor = UIScheme.pfInputBg.toConstraint()
                hoverColor = UIScheme.pfInputBgHovered.toConstraint()
            }
            filterArea.animate {
                setHeightAnimation(Animations.OUT_EXP, 0.5f, 0.pixels)
            }
        }
    }

    override fun onOpenWindow() {
        fetchData()
    }
}
