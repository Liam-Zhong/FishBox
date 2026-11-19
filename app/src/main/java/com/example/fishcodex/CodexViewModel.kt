package com.example.fishcodex

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val fish: List<Fish>, val gameTabs: List<List<String>> = emptyList()) : LoadState
    data class Failed(val message: String) : LoadState
}

/**
 * 筛选条件：搜索词 + 状态(0 全部/1 已集齐/2 未集齐/3 已关注) + 季节、时间、天气、地点、鱼竿。
 * 同一类别内多选为"或"，不同类别之间为"且"。
 * 鱼竿按出没地点推导（池塘/冰洞=池钓，其余地点=路亚），见 Fish.rodKinds。
 */
data class Filters(
    val query: String = "",
    val status: Int = 0,
    val seasons: Set<String> = emptySet(),
    val times: Set<String> = emptySet(),
    val weathers: Set<String> = emptySet(),
    val locations: Set<String> = emptySet(),
    val rods: Set<String> = emptySet(),
) {
    val activeCount: Int get() = seasons.size + times.size + weathers.size + locations.size + rods.size

    /** 筛选面板里的条件是否生效（搜索词不算：搜索框本身就在列表顶部可见） */
    val isActive: Boolean get() = status != 0 || activeCount > 0
}

fun Filters.matches(f: Fish, done: Set<String>, starred: Set<String> = emptySet()): Boolean {
    val okStatus = when (status) {
        1 -> isComplete(f, done)
        2 -> !isComplete(f, done)
        3 -> f.id in starred
        else -> true
    }
    return okStatus &&
        f.matchesQuery(query) &&
        (seasons.isEmpty() || f.seasons.any { it in seasons }) &&
        (times.isEmpty() || f.times.any { it in times }) &&
        (weathers.isEmpty() || f.weather.any { it in weathers }) &&
        (locations.isEmpty() || f.locations.any { it in locations }) &&
        (rods.isEmpty() || f.rodKinds.any { it in rods })
}

/** 等待用户确认的恢复操作 */
class PendingRestore(val data: BackupData, val ignored: Int)

private class Loaded(
    val fish: List<Fish>,
    val gameTabs: List<List<String>>,
    val done: Set<String>,
    val starred: Set<String>,
)

class CodexViewModel(app: Application) : AndroidViewModel(app) {
    private val progressStore = ProgressStore(app)
    private val settingsStore = SettingsStore(app)

    private val _load = MutableStateFlow<LoadState>(LoadState.Loading)
    val load: StateFlow<LoadState> = _load.asStateFlow()

    private val _done = MutableStateFlow<Set<String>>(emptySet())
    val done: StateFlow<Set<String>> = _done.asStateFlow()

    private val _starred = MutableStateFlow<Set<String>>(emptySet())
    val starred: StateFlow<Set<String>> = _starred.asStateFlow()

    private val _settings = MutableStateFlow(settingsStore.load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _filters = MutableStateFlow(Filters())
    val filters: StateFlow<Filters> = _filters.asStateFlow()

    private val _pendingRestore = MutableStateFlow<PendingRestore?>(null)
    val pendingRestore: StateFlow<PendingRestore?> = _pendingRestore.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)

    /** 一次性的提示消息（备份/恢复结果等），界面用提示条展示 */
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        reload()
    }

    /** 在后台线程读取鱼类数据和本地进度 */
    fun reload() {
        _load.value = LoadState.Loading
        viewModelScope.launch {
            try {
                val ctx = getApplication<Application>()
                val loaded = withContext(Dispatchers.IO) {
                    bundledImages = ctx.assets.list("fish_images")?.toSet() ?: emptySet()
                    Loaded(loadFish(ctx), loadGameOrder(ctx), progressStore.load(), progressStore.loadStarred())
                }
                _done.value = loaded.done
                _starred.value = loaded.starred
                _load.value = LoadState.Ready(loaded.fish, loaded.gameTabs)
            } catch (e: Exception) {
                _load.value = LoadState.Failed(e.message ?: e.javaClass.simpleName)
            }
        }
    }

    // ---- 收集进度与关注 ----
    private fun commit(new: Set<String>) {
        _done.value = new
        progressStore.save(new)
    }

    private fun commitStarred(new: Set<String>) {
        _starred.value = new
        progressStore.saveStarred(new)
    }

    fun toggle(fishId: String, quality: Int) {
        val k = key(fishId, quality)
        val cur = _done.value
        commit(if (k in cur) cur - k else cur + k)
    }

    fun toggleStar(fishId: String) {
        val cur = _starred.value
        commitStarred(if (fishId in cur) cur - fishId else cur + fishId)
    }

    fun setAll(fish: Fish, collected: Boolean) {
        val keys = (0 until QUALITY_COUNT).map { key(fish.id, it) }
        val cur = _done.value
        commit(if (collected) cur + keys else cur - keys.toSet())
    }

    fun restore(snapshot: Set<String>) = commit(snapshot)

    fun restoreAll(progress: Set<String>, starred: Set<String>) {
        commit(progress)
        commitStarred(starred)
    }

    fun resetAll() = commit(emptySet())

    // ---- 备份与恢复 ----
    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val text = Backup.encode(_done.value, _starred.value, now)
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    val out = getApplication<Application>().contentResolver.openOutputStream(uri, "wt")
                        ?: error("无法写入所选位置")
                    out.bufferedWriter(Charsets.UTF_8).use { it.write(text) }
                }.isSuccess
            }
            if (ok) {
                updateSettings { it.copy(lastBackupAt = now) }
                _messages.tryEmit("备份已导出")
            } else {
                _messages.tryEmit("导出失败，请换一个位置重试")
            }
        }
    }

    /** 读取并校验备份文件；通过后交给界面让用户确认，确认前不会改动任何数据 */
    fun prepareRestore(uri: Uri) {
        viewModelScope.launch {
            val ids = (_load.value as? LoadState.Ready)?.fish?.map { it.id }?.toSet() ?: emptySet()
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val input = getApplication<Application>().contentResolver.openInputStream(uri)
                        ?: error("无法读取所选文件")
                    val text = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    Backup.decode(text, ids).getOrThrow()
                }
            }
            result
                .onSuccess { _pendingRestore.value = PendingRestore(it.data, it.ignored) }
                .onFailure { _messages.tryEmit("无法恢复：" + (it.message ?: "文件格式不正确")) }
        }
    }

    fun confirmRestore() {
        val p = _pendingRestore.value ?: return
        restoreAll(p.data.progress, p.data.starred)
        _pendingRestore.value = null
    }

    fun cancelRestore() {
        _pendingRestore.value = null
    }

    // ---- 筛选 ----
    fun setQuery(q: String) = _filters.update { it.copy(query = q) }
    fun setStatus(status: Int) = _filters.update { it.copy(status = status) }
    fun toggleSeason(s: String) = _filters.update { it.copy(seasons = it.seasons.toggled(s)) }
    fun toggleTime(t: String) = _filters.update { it.copy(times = it.times.toggled(t)) }
    fun toggleWeather(w: String) = _filters.update { it.copy(weathers = it.weathers.toggled(w)) }
    fun toggleLocation(l: String) = _filters.update { it.copy(locations = it.locations.toggled(l)) }
    fun toggleRod(r: String) = _filters.update { it.copy(rods = it.rods.toggled(r)) }

    /** 清除筛选面板里的条件（保留搜索词） */
    fun clearFilters() = _filters.update { Filters(query = it.query) }

    // ---- 设置 ----
    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }
    fun setDynamicColor(enabled: Boolean) = updateSettings { it.copy(dynamicColor = enabled) }
    fun setCardSize(size: Int) = updateSettings { it.copy(cardSize = size) }
    fun setSortMode(mode: SortMode) = updateSettings { it.copy(sortMode = mode) }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val new = transform(_settings.value)
        _settings.value = new
        settingsStore.save(new)
    }

    private fun Set<String>.toggled(v: String): Set<String> = if (v in this) this - v else this + v
}

private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
