package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.model.data.DataOption
import com.google.gson.annotations.SerializedName

enum class LiquidTypes(val liquid : String) {
    @SerializedName("Lava")
    LAVA("Lava"),
    @SerializedName("Water")
    WATER("Water");

    companion object {
        fun toDataOptions() : ArrayList<DataOption> {
            return LiquidTypes.entries.map { liquid ->
                DataOption(liquid, liquid.liquid)
            } as ArrayList<DataOption>
        }
    }
}