package cloud.glitchdev.rfu.constants

import cloud.glitchdev.rfu.model.data.DataOption
import com.google.gson.annotations.SerializedName

enum class LiquidTypes(val liquid : String) {
    @SerializedName("Lava")
    LAVA("Lava"),
    @SerializedName("Water")
    WATER("Water");

    fun toDataOption() : DataOption {
        return DataOption(this, this.liquid)
    }

    companion object {
        fun toDataOptions() : ArrayList<DataOption> {
            return LiquidTypes.entries.map { liquid ->
                liquid.toDataOption()
            } as ArrayList<DataOption>
        }
    }
}