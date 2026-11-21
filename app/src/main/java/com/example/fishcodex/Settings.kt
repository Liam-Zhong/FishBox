package com.example.fishcodex

import android.content.Context

enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色"),
}

/** 列表排序方式：首字母（默认）/ 搏斗难度 / 游戏内顺序（游戏图鉴的两个页签，每行 3 条） */
enum class SortMode(val label: String) {
    ALPHABET("首字母"),
    FIGHT("搏斗难度"),
    GAME("游戏内顺序"),
}

/** 卡片大小：紧凑（默认，与之前一致）/ 标准 / 大图 */
val CARD_SIZE_LABELS = listOf("紧凑", "标准", "大图")
val CARD_MIN_DP = listOf(88, 112, 140)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val cardSize: Int = 0,
    val lastBackupAt: Long = 0L,
    val sortMode: SortMode = SortMode.ALPHABET,
    /** 搏斗难度排序是否倒序（由难到易） */
    val fightDescending: Boolean = false,
)

class SettingsStore(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load(): AppSettings {
        val modes = ThemeMode.values()
        val idx = prefs.getInt("theme_mode", 0)
        return AppSettings(
            themeMode = if (idx in modes.indices) modes[idx] else ThemeMode.SYSTEM,
            dynamicColor = prefs.getBoolean("dynamic_color", true),
            cardSize = prefs.getInt("card_size", 0).coerceIn(0, CARD_MIN_DP.size - 1),
            lastBackupAt = prefs.getLong("last_backup_at", 0L),
            fightDescending = prefs.getBoolean("fight_desc", false),
            sortMode = SortMode.values().getOrElse(prefs.getInt("sort_mode", 0)) { SortMode.ALPHABET },
        )
    }

    fun save(s: AppSettings) {
        prefs.edit()
            .putInt("theme_mode", s.themeMode.ordinal)
            .putBoolean("dynamic_color", s.dynamicColor)
            .putInt("card_size", s.cardSize)
            .putLong("last_backup_at", s.lastBackupAt)
            .putInt("sort_mode", s.sortMode.ordinal)
            .putBoolean("fight_desc", s.fightDescending)
            .apply()
    }
}
