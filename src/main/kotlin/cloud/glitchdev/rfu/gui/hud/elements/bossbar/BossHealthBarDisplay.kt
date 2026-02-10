    package cloud.glitchdev.rfu.gui.hud.elements.bossbar

    import cloud.glitchdev.rfu.config.categories.GeneralFishing
    import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
    import cloud.glitchdev.rfu.gui.hud.HudElement
    import cloud.glitchdev.rfu.manager.mob.SkyblockEntity
    import gg.essential.elementa.components.UIContainer
    import gg.essential.elementa.constraints.CenterConstraint
    import gg.essential.elementa.constraints.SiblingConstraint
    import gg.essential.elementa.dsl.childOf
    import gg.essential.elementa.dsl.constrain
    import gg.essential.elementa.dsl.percent
    import gg.essential.elementa.dsl.pixels
    import kotlin.math.max

    @HudElement
    object BossHealthBarDisplay : AbstractHudElement("bossHealthBar") {
        val entities: MutableSet<SkyblockEntity> = mutableSetOf()
        val bars : MutableList<BossHealthBar> = mutableListOf()

        override val enabled: Boolean
            get() = (super.enabled || entities.isNotEmpty()) && GeneralFishing.bossHealthBars

        val barsContainer = UIContainer().constrain {
            width = (400 * scale).pixels()
            height = (9 * scale).pixels()
        } childOf this

        override fun onUpdateState() {
            if (isEditing) {
                getOrAddBar(0).forceRendering = true
            }

            entities.forEachIndexed { index, entity ->
                val bar = getOrAddBar(index)
                bar.updateEntity(entity)
            }

            bars.forEachIndexed { index, bar ->
                bar.updateScale(scale)
                bar.constrain {
                    y = SiblingConstraint(2 * scale)
                    height = (9 * scale).pixels()
                }

                if(index < entities.size) return@forEachIndexed
                bar.updateEntity(null)
            }

            barsContainer.constrain {
                width = (400 * scale).pixels()
                if (!isEditing) {
                    height = ((2 * (max(entities.size-1, 0)) + 9 * (entities.size)) * scale).pixels()
                } else {
                    height = ((2 * (max(entities.size-1, 0)) + 9 * (max(entities.size, 1))) * scale).pixels()
                }
            }
        }

        private fun getOrAddBar(index : Int) : BossHealthBar {
            val bar = bars.getOrElse(index) {
                val bossBar = BossHealthBar(null)
                bars.add(bossBar)
                bossBar.constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint(2 * scale)
                    width = 100.percent()
                    height = (9 * scale).pixels()
                } childOf barsContainer
            }

            return bar
        }

        fun updateEntities(entities : Set<SkyblockEntity>) {
            this.entities.clear()
            this.entities.addAll(entities)
            updateState()
        }
    }