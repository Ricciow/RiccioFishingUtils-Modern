package cloud.glitchdev.rfu.achievement

import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.data.achievements.AchievementHandler
import cloud.glitchdev.rfu.events.AbstractEventManager

abstract class BaseAchievement : IAchievement {
    protected val activeListeners = mutableListOf<AbstractEventManager.ManagedTask<*, *>>()
    
    private var _isCompleted: Boolean = false
    override val isCompleted: Boolean
        get() = _isCompleted || AchievementManager.isCompleted(id)
        
    protected open var _progress: Float = 0.0f
    override val progress: Float
        get() = if (isCompleted) 1.0f else _progress

    fun init() {
        AchievementManager.register(this)
        
        val data = AchievementHandler.getAchievementData(id)
        if (data != null) {
            _isCompleted = data.isCompleted
            if (data.progressData.isNotEmpty()) {
                loadState(data.progressData)
            }
        }
        
        if (!isCompleted) {
            setupListeners()
        }
    }

    abstract fun setupListeners()

    protected open fun complete() {
        if (_isCompleted) return
        _isCompleted = true
        unregisterAllListeners()
        AchievementProvider.fireAchievementUnlocked(this)
    }

    open fun debugComplete() {
        complete()
    }

    open fun debugReset() {
        _isCompleted = false
        _progress = 0.0f
        unregisterAllListeners()
        loadState(emptyMap())
        setupListeners()
    }

    protected fun unregisterAllListeners() {
        for (listener in activeListeners) {
            listener.unregister()
        }
        activeListeners.clear()
    }
    
    open fun loadState(progressData: Map<String, Any>) {}
    open fun saveState(): Map<String, Any> = emptyMap()
}
