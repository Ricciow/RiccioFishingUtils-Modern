package cloud.glitchdev.rfu.utils.network.api

import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.model.pets.ItemRarity
import cloud.glitchdev.rfu.model.pets.PetAuctionResponse
import cloud.glitchdev.rfu.model.pets.PetCategory
import cloud.glitchdev.rfu.utils.network.Network
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PetsApi {
    private val gson = Gson()

    fun getBestPets(
        category: PetCategory? = null,
        rarity: ItemRarity? = null,
        count: Int = 10,
        filterCandy: Boolean = true,
        unique: Boolean = false,
        maxLevel: Int? = null,
        callback: (List<PetAuctionResponse>) -> Unit
    ) {
        val queryParams = mutableListOf<String>()
        category?.let { queryParams.add("category=${it.name}") }
        rarity?.let { queryParams.add("rarity=${it.name}") }
        queryParams.add("count=${count.coerceAtMost(25)}")
        queryParams.add("filterCandy=$filterCandy")
        queryParams.add("unique=$unique")
        maxLevel?.let { queryParams.add("maxLevel=$it") }

        val queryString = if (queryParams.isNotEmpty()) "?" + queryParams.joinToString("&") else ""
        val url = "$API_URL/v1/auctions/pets/best$queryString"

        Network.getRequest(url, useToken = true) { response ->
            if (response.isSuccessful() && response.body != null) {
                try {
                    val listType = object : TypeToken<List<PetAuctionResponse>>() {}.type
                    val pets: List<PetAuctionResponse> = gson.fromJson(response.body, listType)
                    callback(pets)
                } catch (e: Exception) {
                    callback(emptyList())
                }
            } else {
                callback(emptyList())
            }
        }
    }
}
