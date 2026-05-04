package cloud.glitchdev.rfu.model.pets

data class PetAuctionResponse(
    val uuid: String,
    val petName: String,
    val category: PetCategory,
    val rarity: ItemRarity,
    val level: Int,
    val candyUsed: Int,
    val price: Long,
    val coinsPerExp: Double,
    val xpNeeded: Double,
    val profit: Double,
    val lvl100Cost: Long
)
