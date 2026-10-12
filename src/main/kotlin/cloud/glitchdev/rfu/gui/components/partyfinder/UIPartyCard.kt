package cloud.glitchdev.rfu.gui.components.partyfinder

import cloud.glitchdev.rfu.constants.fishing.LiquidTypes
import cloud.glitchdev.rfu.constants.chat.PartyTypes
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextEffects
import cloud.glitchdev.rfu.events.managers.PartyFinderEvents
import cloud.glitchdev.rfu.feature.other.ignore.IgnoreUtils
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.components.UIPopup
import cloud.glitchdev.rfu.gui.components.colors
import cloud.glitchdev.rfu.gui.window.PartyFinderWindow
import cloud.glitchdev.rfu.model.party.FishingParty
import cloud.glitchdev.rfu.gui.components.elementa.BoundingBoxConstraint
import cloud.glitchdev.rfu.gui.components.elementa.CopyComponentSizeConstraint
import cloud.glitchdev.rfu.gui.components.elementa.group.GroupMaxSizeConstraint
import cloud.glitchdev.rfu.gui.components.elementa.RFUWrappedText
import cloud.glitchdev.rfu.gui.components.elementa.UIVanillaTooltip
import cloud.glitchdev.rfu.model.data.DataOption
import cloud.glitchdev.rfu.party.PartyRequirementsManager
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.dsl.isUser
import cloud.glitchdev.rfu.utils.dsl.compact
import cloud.glitchdev.rfu.utils.network.PartyWebSocket
import cloud.glitchdev.rfu.utils.network.WebSocketClient
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.MinConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.basicHeightConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.div
import gg.essential.elementa.dsl.max
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.times
import gg.essential.elementa.dsl.toConstraint
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import java.util.Locale

class UIPartyCard(
    val party: FishingParty,
    val radiusProps: Float,
    var postConfirmationText: String? = null,
    val wasHovered: Boolean = false,
    val showNoRequirements: Boolean = false,
) : UIRoundedRectangle(radiusProps) {
    val borderWidth = UIScheme.pfCardBorderWidth
    val innerPadding = UIScheme.pfCardInnerPadding
    private val joinErrorPopup: UIPopup get() = PartyFinderWindow.popup
    lateinit var titleText : UIText
    lateinit var innerContainer : UIContainer
    lateinit var levelBadge : UIPartyBadge
    lateinit var memberBadge : UIPartyBadge
    lateinit var descriptionSeparator : UIBlock
    lateinit var actionButton : UIButton
    private var blockButton: UIButton? = null
    private val tagsContainer = UIContainer()
    private val maxDescriptionLines = 2

    init {
        create()
    }

    fun create() {
        this.constrain {
            color = (if (wasHovered) UIScheme.pfCardBorderHovered else UIScheme.pfCardBorder).toConstraint()
            height = BoundingBoxConstraint() + borderWidth.pixels //Not 2x because bounding box accounts for padding
        }.onMouseEnter {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorderHovered.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleHoverColor.toConstraint())
            }
            levelBadge.animateHover()
            memberBadge.animateHover()
            descriptionSeparator.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardSeparatorHover.toConstraint())
            }
            actionButton.unhide()
            blockButton?.unhide()
        }.onMouseLeave {
            animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardBorder.toConstraint())
            }
            titleText.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardTitleColor.toConstraint())
            }
            levelBadge.animateNormal()
            memberBadge.animateNormal()
            descriptionSeparator.animate {
                setColorAnimation(Animations.IN_EXP, UIScheme.HOVER_EFFECT_DURATION, UIScheme.pfCardSeparator.toConstraint())
            }
            actionButton.hide(true)
            blockButton?.hide(true)
        }.onMouseClick {
            if (!WebSocketClient.isConnected) {
                joinErrorPopup.showWarning("Not connected to RFU Backend!")
                return@onMouseClick
            } else if (party.user.isUser()) {
                joinErrorPopup.showWarning("You are already the leader of this party!")
            } else if (Party.members.contains(party.user)) {
                joinErrorPopup.showWarning("You are already in this party!")
            } else if (party.players.current >= party.players.max) {
                joinErrorPopup.showWarning("This party is full!")
            } else if (PartyWebSocket.myParty != null) {
                joinErrorPopup.showWarning("You are already hosting a party! Please delete your current party listing before joining another.")
            } else {
                val validation = PartyRequirementsManager.canJoinParty(party)
                if (!validation.isSuccess) {
                    joinErrorPopup.showWarning(validation.getErrorMessage())
                    return@onMouseClick
                }
                PartyWebSocket.joinParty(party.user)
            }
        }

        val innerBg = UIRoundedRectangle(radiusProps).constrain {
            x = CenterConstraint()
            y = borderWidth.pixels
            width = max(0.pixels, 100.percent - (borderWidth * 2).pixels)
            height = ChildBasedSizeConstraint() + (innerPadding * 2).pixels
            color = UIScheme.pfCardBg.toConstraint()
        } childOf this

        innerContainer = UIContainer().constrain {
            x = innerPadding.pixels
            y = innerPadding.pixels
            width = max(0.pixels, 100.percent - (innerPadding * 2).pixels)
            height = BoundingBoxConstraint()
        } childOf innerBg

        createHeader()
        createDescription()
        createTags()

        if (wasHovered) {
            actionButton.unhide()
            blockButton?.unhide()
            titleText.constrain { color = UIScheme.pfCardTitleHoverColor.toConstraint() }
            levelBadge.constrain { color = UIScheme.pfCardLevelBorderHoveredColor.toConstraint() }
            levelBadge.valueText.constrain { color = UIScheme.pfCardTitleHoverColor.toConstraint() }
            memberBadge.constrain { color = UIScheme.pfCardLevelBorderHoveredColor.toConstraint() }
            memberBadge.valueText.constrain { color = UIScheme.pfCardTitleHoverColor.toConstraint() }
            descriptionSeparator.constrain { color = UIScheme.pfCardSeparatorHover.toConstraint() }
        }
    }

    fun createHeader() {
        val header = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = 100.percent
            height = BoundingBoxConstraint()
        } childOf innerContainer

        val textContainer = UIContainer().constrain {
            x = 0.pixels
            y = 0.pixels
            width = BoundingBoxConstraint()
            height = BoundingBoxConstraint()
        } childOf header

        val userRow = UIContainer().constrain {
            x = 0.pixels
            y = 0.pixels
            width = BoundingBoxConstraint()
        } childOf textContainer
        val userText = UIText(party.user) childOf userRow
        val title = party.title
            .takeIf { it.isNotEmpty() }
            ?.let { TextEffects.BOLD.toString() + it }
            ?: party.island.island

        titleText = UIText(title).constrain {
            x = 0.pixels
            y = SiblingConstraint(UIScheme.pfCardSmallPadding)
            width = MinConstraint(ScaledTextConstraint(1f), 70.percent)
            height = TextAspectConstraint()
        } childOf textContainer

        userRow.constrain {
            height = CopyComponentSizeConstraint(titleText) * 0.75
        }
        userText.constrain {
            x = 0.pixels
            y = 0.pixels
            width = TextAspectConstraint()
            height = 100.percent
            color = UIScheme.pfCardUserColor.toConstraint()
        }
        createListingAction(userRow, userText)

        val isFull = party.players.current >= party.players.max
        memberBadge = UIPartyBadge(
            if (isFull) "${TextColor.LIGHT_RED}FULL" else "PARTY",
            (if (isFull) TextColor.LIGHT_RED.toString() else "") + party.players.getString(),
            titleText
        ).constrain {
            y = 0.pixels
            x = 0.pixels(true)
            width = AspectConstraint(1f)
            height = CopyComponentSizeConstraint(textContainer)
        } childOf header

        val overflow = party.requiredOverflow()
        levelBadge = UIPartyBadge(
            if (overflow > 0) "OVERFL" else "LEVEL",
            if (overflow > 0) overflow.compact() else "${party.level.coerceAtMost(50)}",
            titleText
        ).constrain {
            y = 0.pixels
            x = SiblingConstraint(UIScheme.pfCardSmallPadding, alignOpposite = true)
            width = AspectConstraint(1f)
            height = CopyComponentSizeConstraint(textContainer)
        } childOf header

        if (overflow > 0) {
            UIVanillaTooltip(Component.literal("Overflow XP required: ${String.format(Locale.US, "%,d", overflow)}")).constrain {
                width = 100.percent
                height = 100.percent
            } childOf levelBadge
        }

        if(party.level == 0 && overflow == 0L) {
            levelBadge.hide()
        }
    }

    fun createDescription() {
        val descriptionContainer = UIVanillaTooltip(emptyList<Component>()).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(UIScheme.pfCardInnerPadding)
            width = 100.percent
        } childOf innerContainer

        descriptionSeparator = UIBlock() childOf descriptionContainer

        val descriptionText = party.description
            .takeIf { it.isNotEmpty() }
            ?: "${party.island.island} fishing."

        val description = RFUWrappedText(descriptionText, maxLines = maxDescriptionLines).constrain {
            x = SiblingConstraint(UIScheme.pfCardSmallPadding)
            y = CenterConstraint()
            width = FillConstraint()
            textScale = CopyComponentSizeConstraint(titleText) * 0.8 / 9
            color = UIScheme.pfCardDescriptionColor.toConstraint()
        } childOf descriptionContainer

        descriptionContainer.tooltip = {
            val lines = description.getFullWrappedLines()
            if (lines.size > maxDescriptionLines) {
                listOf(Component.literal("Description:").withStyle(ChatFormatting.DARK_GRAY)) +
                    lines.map { Component.literal(it).withStyle(ChatFormatting.GRAY) }
            } else emptyList()
        }

        descriptionContainer.constrain {
            val previewHeight = basicHeightConstraint { description.getHeightForLines(maxDescriptionLines) } +
                (UIScheme.pfCardSmallPadding * 2).pixels
            val tagsHeight = ChildBasedSizeConstraint().apply { constrainTo = tagsContainer }
            height = GroupMaxSizeConstraint("PartyCardDescription", previewHeight) +
                GroupMaxSizeConstraint("PartyCardTags", tagsHeight) - tagsHeight
        }

        descriptionSeparator.constrain {
            x = SiblingConstraint(UIScheme.pfCardSmallPadding)
            y = 0.pixels
            width = 1.pixels
            height = CopyComponentSizeConstraint(descriptionContainer)
            color = UIScheme.pfCardSeparator.toConstraint()
        }
    }

    fun createTags() {
        tagsContainer.constrain {
            x = CenterConstraint()
            y = SiblingConstraint(UIScheme.pfCardInnerPadding)
            width = 100.percent
            height = ChildBasedSizeConstraint()
        } childOf innerContainer

        val locationContainer = UIContainer().constrain {
            x = 0.pixels
            y = 0.pixels
            width = 100.percent
            height = BoundingBoxConstraint()
        } childOf tagsContainer

        UIConditionCard(DataOption("location", party.island.island)).constrain {
            x = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
            y = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
        } childOf locationContainer

        val typeOption = when (party.fishingType) {
            PartyTypes.REGULAR -> if (party.liquid == LiquidTypes.WATER) {
                DataOption("water", "Water")
            } else {
                DataOption("lava", "Lava")
            }
            else -> DataOption(party.fishingType.name.lowercase(), party.fishingType.type)
        }
        UIConditionCard(typeOption).constrain {
            x = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
            y = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
        } childOf locationContainer

        party.requisites.firstOrNull { it.id == "has_killer" && it.value }?.let { requisite ->
            UIConditionCard(requisite.toDataOption()).constrain {
                x = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
                y = CramSiblingConstraint(UIScheme.pfCardSmallPadding)
            } childOf locationContainer
        }

        val activeRequisites = party.requisites
            .filter { it.value && it.id != "has_killer" }
            .sortedBy {
                when (it.id) {
                    "looting_5" -> 0
                    "bloodshot" -> 1
                    "enderman_9" -> 2
                    "brain_food" -> 3
                    "wither_impact" -> 4
                    else -> 5
                }
            }
        if (activeRequisites.isEmpty() && !showNoRequirements) return

        val requirementsContainer = UIContainer().constrain {
            x = 0.pixels
            y = SiblingConstraint(UIScheme.pfCardInnerPadding)
            width = 100.percent
            height = (4.5f + (UIScheme.pfConditionCardPadding + UIScheme.pfConditionCardWidth) * 2).pixels
        } childOf tagsContainer

        if (activeRequisites.isNotEmpty()) {
            UICompactRequirements(activeRequisites.map { it.toDataOption() }).constrain {
                x = 0.pixels
                y = 0.pixels
                width = 100.percent
                height = 100.percent
            } childOf requirementsContainer
        } else if (showNoRequirements) {
            UIText("No requirements").constrain {
                x = 0.pixels
                y = CenterConstraint()
                width = MinConstraint(ScaledTextConstraint(0.5f), 100.percent)
                height = TextAspectConstraint()
                color = UIScheme.pfCardUserColor.toConstraint()
            } childOf requirementsContainer
        }
    }

    private fun createListingAction(parent: UIContainer, userText: UIText) {
        val isUser = party.user.isUser()
        if (!isUser) {
            blockButton = createListingButton(parent, userText, "block", "Block player") {
                showBlockConfirmation()
            }
        }
        val icon = if(isUser) "delete" else "report"
        actionButton = createListingButton(parent, userText, icon, if (isUser) "Delete listing" else "Report listing") {
            val action = if (isUser) "delete your party?" else "report ${party.user}'s party?\nThis is only for the party finder listing, not what happens inside the party, use hypixel's reporting for that."
            val pcText = postConfirmationText ?: if (isUser) null else "Party reported"
            PartyFinderWindow.popup.showWarning("Are you sure you want to $action", pcText) {
                if (isUser) {
                    PartyWebSocket.deleteParty(party.user)
                } else {
                    if (!WebSocketClient.isConnected) {
                        PartyFinderWindow.popup.showWarning("Not connected to RFU Backend!")
                        return@showWarning
                    }
                    PartyWebSocket.reportParty(party.user)
                    showBlockConfirmation("${postConfirmationText ?: "Report sent."}\nWould you also like to block ${party.user}?")
                }
            }
        }
    }

    private fun showBlockConfirmation(prompt: String = "Are you sure you want to block ${party.user}?") {
        PartyFinderWindow.popup.showWarning(
            "$prompt\nThis adds them to your RFU ignore list.",
            "Blocked ${party.user}.\nUnblock with /rfuignore remove ${party.user}."
        ) {
            val entry = IgnoreUtils.getIgnoredEntry()
            if (entry.add(party.user)) IgnoreUtils.saveIgnoredEntry(entry)
            PartyFinderEvents.refreshParties()
        }
    }

    private fun createListingButton(
        parent: UIContainer,
        userText: UIText,
        icon: String,
        tooltip: String,
        onClick: () -> Unit,
    ): UIButton {
        val actionSlot = UIVanillaTooltip(Component.literal(tooltip)).constrain {
            x = SiblingConstraint(UIScheme.pfCardTinyPadding)
            y = CenterConstraint()
            width = AspectConstraint(1f)
            height = CopyComponentSizeConstraint(userText) * 1.5
        } childOf parent
        val imagePath = "/assets/rfu/ui/$icon.png"
        val image = javaClass.getResource(imagePath)?.let { UIImage.ofResourceCached(imagePath) }
        val button = if (image != null) {
            UIButton.withImage(image, 2f, onClick = onClick)
        } else {
            UIButton("×", 2f, baseTextScale = 0.75f, onClick = onClick)
        }
        button.constrain {
            x = 0.pixels
            y = 0.pixels
            width = 100.percent
            height = 100.percent
        } childOf actionSlot
        button.colors {
            primaryColor = UIScheme.transparent.toConstraint()
            hoverColor = UIScheme.transparent.toConstraint()
            textColor = UIScheme.pfCardUserColor.toConstraint()
            hoverTextColor = UIScheme.pfCardOverlayHoverColor.toConstraint()
        }
        button.hide(true)
        return button
    }
}
