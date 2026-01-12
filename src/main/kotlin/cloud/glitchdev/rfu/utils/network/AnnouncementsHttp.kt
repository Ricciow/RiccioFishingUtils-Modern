package cloud.glitchdev.rfu.utils.network

import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.model.announcement.Announcement
import cloud.glitchdev.rfu.utils.network.Network.getRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mojang.util.InstantTypeAdapter
import java.time.Instant

object AnnouncementsHttp {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
        .create()

    fun getLatestAnnouncement(callback : (Announcement?) -> Unit) {
        getRequest("${API_URL}/announcement/latest") { response ->
            if(!response.isSuccessful()) {
                callback(null)
                return@getRequest
            }

            try {
                val latestAnnouncement = gson.fromJson(response.body, Announcement::class.java)
                callback(latestAnnouncement)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }
}