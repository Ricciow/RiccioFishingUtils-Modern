package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.feature.Feature
import java.util.ServiceLoader

object Features {
    fun initializeFeatures() {
        val features = ServiceLoader.load(Feature::class.java, Feature::class.java.classLoader)

        features.forEach { feature ->
            feature.onInitialize()
        }
    }
}