package cloud.glitchdev.rfu.gui.hud.elements.bossbar

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.manager.mob.SkyblockEntity
import cloud.glitchdev.rfu.utils.dsl.parseHealthValue
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class BossHealthBar(
    var entity: SkyblockEntity?,
) : UIContainer() {
    var scale = 1f
    val name = UIText() childOf this
    var barBg = UIRoundedRectangle(5f) childOf this
    var barContainer = UIContainer() childOf barBg
    var bar = UIRoundedRectangle(3f) childOf barContainer
    val healthText = UIText() childOf this
    var forceRendering = false
        set(value) {
            field = value
            updateState()
        }

    fun updateState() {
        this.setHidden(entity == null && !forceRendering)
        val health = entity?.health ?: "0"
        val maxHealth = entity?.maxHealth ?: "1"
        val isShurikened = entity?.isShurikened ?: false
        val healthPercentage = health.parseHealthValue().toFloat() / maxHealth.parseHealthValue().toFloat() * 100
        val themeColor = when {
            isShurikened && GeneralFishing.coloredShurikenBar -> UIScheme.barShuriken
            healthPercentage > 50 -> UIScheme.barHighHP
            healthPercentage > 25 -> UIScheme.barMediumHP
            healthPercentage > 0 -> UIScheme.barLowHP
            else -> Color.white
        }.toConstraint()

        name.constrain {
            x = SiblingConstraint(2 * scale)
            y = CenterConstraint()
            width = ScaledTextConstraint(scale)
            height = TextAspectConstraint()
            color = themeColor
        }

        val displayName = buildString {
            append(entity?.sbName ?: "Example Mob")
            if(entity?.isShurikened ?: false) append(" ✯")
        }

        name.setText(displayName)
        barBg.constrain {
            x = SiblingConstraint(2 * scale)
            y = CenterConstraint()
            width = FillConstraint() - (4 * scale).pixels()
            height = (scale * 9).pixels()
            color = Color.BLACK.toConstraint()
        }

        barContainer.constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - (2 * scale).pixels()
            height = 100.percent() - (2 * scale).pixels()
        }

        bar.constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = healthPercentage.percent()
            height = 100.percent()
            color = themeColor
        }



        healthText.constrain {
            x = SiblingConstraint(2 * scale)
            y = CenterConstraint()
            width = ScaledTextConstraint(scale)
            height = TextAspectConstraint()
            color = themeColor
        }

        if(entity?.outdatedNametag() ?: true) {
            healthText.setText("${" ".repeat(max(maxHealth.length - min(health.length, 3), 0))}${"?".repeat(min(health.length, 3))} / $maxHealth ❤")
        } else {
            healthText.setText("${" ".repeat(max(maxHealth.length - health.length, 0))}$health / $maxHealth ❤")
        }
    }

    fun updateScale(scale: Float) {
        this.scale = scale
        updateState()
    }

    fun updateEntity(entity: SkyblockEntity?) {
        this.entity = entity
        updateState()
    }
}