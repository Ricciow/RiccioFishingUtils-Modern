package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.constants.Bait
import cloud.glitchdev.rfu.events.managers.SetSlotEvents.registerSetSlotEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents

@RFUFeature
object BaitManager : Feature {
    var lastBait: Bait? = null
    private var currentBait : Bait? = null
    var lastCount : Int? = null
    private var currentCount : Int? = null

    private const val PLAYER_INVENTORY_ID = 0
    private val BAIT_COUNT_REGEX = """Bait Remaining: ([\d,]+)""".toExactRegex()

    override fun onInitialize() {
        registerSetSlotEvent { id , slot, item ->
            if (id != PLAYER_INVENTORY_ID || item.isEmpty) return@registerSetSlotEvent
            if (slot != 44) return@registerSetSlotEvent
            val bait = Bait.fromName(item.hoverName.string)

            if(bait == null) {
                lastBait = null
                currentBait = null
                lastCount = null
                currentCount = null
                return@registerSetSlotEvent
            }

            if(lastBait == null) {
                lastBait = bait
            } else {
                lastBait = currentBait
            }

            lastBait = bait

            val loreLines = item[DataComponents.LORE]?.lines ?: return@registerSetSlotEvent
            val count = loreLines.mapNotNull {
                BAIT_COUNT_REGEX.find(it.toUnformattedString())?.groupValues?.getOrNull(1)?.replace(",", "")
            }.getOrNull(0)?.toIntOrNull() ?: return@registerSetSlotEvent

            if(lastCount == null || (lastCount ?: 0) <= count ) {
                lastCount = count
            } else {
                lastCount = currentCount
            }

            currentCount = count
        }
    }
}
