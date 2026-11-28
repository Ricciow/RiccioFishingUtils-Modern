package cloud.glitchdev.rfu.achievement

import cloud.glitchdev.rfu.achievement.interfaces.IAchievement
import cloud.glitchdev.rfu.data.achievements.AchievementHandler
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.EventScopes

abstract class BaseAchievement : IAchievement {
    private val listenerScopes = EventScopes()

    private val isLegacy: Boolean
        get() = category == AchievementCategory.LEGACY
    private var isApplyingDebugChange = false
    
    private var _isCompleted: Boolean = false
    override val isCompleted: Boolean
        get() = _isCompleted

    private var _isCheated: Boolean = false
    override val isCheated: Boolean
        get() = _isCheated
        
    protected open var _progress: Float = 0.0f
    override val progress: Float
        get() = if (isCompleted) 1.0f else _progress

    fun init() {
        AchievementManager.register(this)
        
        val data = AchievementHandler.getAchievementData(id)
        if (data != null) {
            _isCompleted = data.isCompleted
            _isCheated = data.isCheated
            if (data.progressData.isNotEmpty()) {
                loadState(data.progressData)
            }
        }
        
        if (!isCompleted && !isLegacy) {
            startListeners()
        }
    }

    fun reloadState() {
        unregisterAllListeners()
        _progress = 0.0f
        val data = AchievementHandler.getAchievementData(id)
        if (data != null) {
            _isCompleted = data.isCompleted
            _isCheated = data.isCheated
            if (data.progressData.isNotEmpty()) {
                loadState(data.progressData)
            } else {
                loadState(emptyMap())
            }
        } else {
            _isCompleted = false
            _isCheated = false
            loadState(emptyMap())
        }
    }

    fun postReload() {
        if (!isCompleted && !isLegacy) {
            startListeners()
        }
    }

    abstract fun setupListeners(scope: EventScope)

    private fun startListeners() {
        listenerScopes.load(::setupListeners)
    }

    protected open fun complete() {
        if (_isCompleted || (isLegacy && !isApplyingDebugChange)) return
        _isCompleted = true
        unregisterAllListeners()
        AchievementProvider.fireAchievementUnlocked(this)
    }

    open fun debugComplete() {
        applyDebugChange { complete() }
    }

    open fun debugReset() {
        _isCompleted = false
        _isCheated = false
        _progress = 0.0f
        unregisterAllListeners()
        loadState(emptyMap())
        if (!isLegacy) {
            startListeners()
        }
        AchievementProvider.fireAchievementUpdated(this)
    }

    protected fun applyDebugChange(change: () -> Unit) {
        _isCheated = true
        val wasApplyingDebugChange = isApplyingDebugChange
        isApplyingDebugChange = true
        try {
            change()
        } finally {
            isApplyingDebugChange = wasApplyingDebugChange
        }
    }

    protected fun unregisterAllListeners() {
        listenerScopes.close()
    }
    
    open fun loadState(progressData: Map<String, Any>) {}
    open fun saveState(): Map<String, Any> = emptyMap()
}
