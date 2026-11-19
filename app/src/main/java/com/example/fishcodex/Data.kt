package com.example.fishcodex

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import org.json.JSONArray
import org.json.JSONObject

const val QUALITY_COUNT = 5
val QUALITY_IDS = listOf("standard", "uncommon", "rare", "epic", "legendary")
val QUALITY_NAMES = listOf("标准", "非凡", "稀有", "史诗", "传奇")
val QUALITY_COLORS = listOf(
    Color(0xFFB0B0B0), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFFFB300)
)

val LOC_ZH = mapOf(
    "boulder" to "巨石", "dock" to "码头", "ice_hole" to "冰洞", "midstream" to "河中央",
    "pond" to "池塘", "river_bend" to "河湾", "riverside" to "岸边", "wreckage" to "沉船",
)
val SEASON_ZH = mapOf("spring" to "春天", "summer" to "夏天", "autumn" to "秋天", "winter" to "冬天")
val WEATHER_ZH = mapOf(
    "foggy" to "雾天", "heavy_rain" to "大雨", "heavy_snow" to "大雪", "light_rain" to "小雨",
    "light_snow" to "小雪", "meteor_shower" to "流星雨", "sunny" to "晴天",
)
val TIME_ZH = mapOf(
    "dawn" to "清晨", "morning" to "上午", "afternoon" to "下午",
    "dusk" to "黄昏", "night" to "深夜", "after_midnight" to "凌晨",
)
val ROD_ZH = mapOf("lure" to "路亚", "pond" to "池钓", "simple" to "简易")
/** 筛选里的鱼竿选项（简易竿与地点无关，不参与按地点推导） */
val ROD_FILTER_ORDER = listOf("lure", "pond")
val SEASON_ORDER = listOf("spring", "summer", "autumn", "winter")
val LOC_ORDER = listOf("riverside", "dock", "river_bend", "midstream", "boulder", "wreckage", "pond", "ice_hole")
val TIME_ORDER = listOf("after_midnight", "dawn", "morning", "afternoon", "dusk", "night")
val WEATHER_ORDER = listOf("sunny", "foggy", "light_rain", "heavy_rain", "light_snow", "heavy_snow", "meteor_shower")

data class Fish(
    val id: String,
    val name: String,
    val pinyin: String,
    val initial: Char,
    val description: String,
    val tag: String,
    val rods: List<String>,
    val gear: String?,
    val fight: Int,
    val locations: List<String>,
    val times: List<String>,
    val weather: List<String>,
    val seasons: List<String>,
    val prices: List<Int>,
    val rarityNames: List<String>?,
    /** 按出没地点推导出的鱼竿类型（lure=路亚，pond=池钓）；站点原始的 rods 字段原样保留 */
    val rodKinds: Set<String> = emptySet(),
    val rarityDescs: List<String>? = null,
    val biteTier: String? = null,
    val isNew: Boolean = false,
    val detailUrl: String = "",
    val body: Color,
    val fin: Color,
    val shape: Int,
)

/** assets/fish_images 里已有的文件名（启动时扫描一次） */
var bundledImages: Set<String> = emptySet()

/** 鱼图全部来自打包在 App 里的本地文件（离线可用，不联网下载）；缺图时返回 null，界面显示占位鱼 */
fun fishImageModel(id: String, quality: Int): String? {
    val file = "${id}_${QUALITY_IDS[quality]}.webp"
    return if (file in bundledImages) "file:///android_asset/fish_images/$file" else null
}

/** 可选字体：把字体文件命名为 app_font.ttf 放到 assets/fonts/，没有就用系统字体 */
fun loadAppTypeface(ctx: Context): Typeface? = try {
    Typeface.createFromAsset(ctx.assets, "fonts/app_font.ttf")
} catch (e: Exception) {
    null
}

fun loadAppFont(ctx: Context): FontFamily? = loadAppTypeface(ctx)?.let { FontFamily(it) }

private fun JSONArray.strings(): List<String> = List(length()) { getString(it) }

private val SEASON_SHORT = mapOf("spring" to "春", "summer" to "夏", "autumn" to "秋", "winter" to "冬")

/** 读取 assets/fish.json（真实数据，来源见 data/manifest.json） */
/** locations.json 里每个地点使用的鱼竿类型；文件缺失或损坏时用内置的默认对应（池塘/冰洞=池钓，其余=路亚） */
fun loadLocationRods(ctx: Context): Map<String, List<String>> {
    val fallback = LOC_ORDER.associateWith { if (it == "pond" || it == "ice_hole") listOf("pond") else listOf("lure") }
    return try {
        val arr = JSONArray(ctx.assets.open("locations.json").bufferedReader().use { it.readText() })
        val map = HashMap<String, List<String>>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val rods = o.optJSONArray("rods")?.strings()
            if (!rods.isNullOrEmpty()) map[o.getString("id")] = rods
        }
        if (map.isEmpty()) fallback else fallback + map
    } catch (e: Exception) {
        fallback
    }
}

/** game_order.json：游戏内图鉴的页签与顺序，返回每个页签的鱼 id 列表；缺失时为空（界面回退到按字母排序） */
fun loadGameOrder(ctx: Context): List<List<String>> = try {
    val tabs = JSONObject(ctx.assets.open("game_order.json").bufferedReader().use { it.readText() }).getJSONArray("tabs")
    List(tabs.length()) { tabs.getJSONObject(it).getJSONArray("fish").strings() }
} catch (e: Exception) {
    emptyList()
}

fun loadFish(ctx: Context): List<Fish> {
    val locRods = loadLocationRods(ctx)
    val text = ctx.assets.open("fish.json").bufferedReader().use { it.readText() }
    val arr = JSONArray(text)
    val list = List(arr.length()) { i ->
        val o: JSONObject = arr.getJSONObject(i)
        val id = o.getString("id")
        val seasons = o.getJSONArray("seasons").strings()
        val pr = o.getJSONObject("sell_price_treats")
        val rn = o.optJSONObject("rarity_names_zh")
        val rd = o.optJSONObject("rarity_descriptions_zh")
        val h = (Math.abs(id.hashCode()) % 360).toFloat()
        val locations = o.getJSONArray("locations").strings()
        Fish(
            id = id,
            name = o.getJSONObject("name").getString("zh-CN"),
            pinyin = o.optString("pinyin", ""),
            initial = o.optString("pinyin", "").firstOrNull()?.uppercaseChar() ?: '#',
            description = o.getJSONObject("description").getString("zh-CN"),
            tag = seasons.joinToString("") { SEASON_SHORT[it] ?: "" },
            rods = o.getJSONArray("rods").strings(),
            gear = if (o.isNull("gear")) null else o.getString("gear"),
            fight = o.getInt("fight"),
            locations = locations,
            times = o.getJSONArray("times").strings(),
            weather = o.getJSONArray("weather").strings(),
            seasons = seasons,
            prices = QUALITY_IDS.map { pr.getInt(it) },
            rarityNames = rn?.let { r -> QUALITY_IDS.map { r.getString(it) } },
            rodKinds = locations.flatMap { locRods[it].orEmpty() }.toSet(),
            rarityDescs = rd?.let { r -> QUALITY_IDS.map { r.optString(it, "") } },
            biteTier = if (o.isNull("bite_tier_zh")) null else o.optString("bite_tier_zh"),
            isNew = o.optBoolean("is_new", false),
            detailUrl = o.optString("detail_url", ""),
            body = Color.hsv(h, 0.55f, 0.85f),
            fin = Color.hsv(h, 0.70f, 0.60f),
            shape = Math.abs(id.hashCode() / 7) % 3,
        )
    }
    // 按拼音首字母排序（pinyin 字段为人工标注）
    return list.sortedWith(compareBy({ it.pinyin.replace(" ", "") }, { it.id }))
}

fun key(fishId: String, q: Int) = "$fishId:$q"

/** 收集进度、关注列表存本地。进度 key 形如 "shimmerfin:3"（该鱼第 4 个品质已收集）；关注列表是鱼的 id 集合。 */
class ProgressStore(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("progress", Context.MODE_PRIVATE)

    fun load(): Set<String> = prefs.getStringSet("done", emptySet())!!.toSet()

    fun save(set: Set<String>) {
        prefs.edit().putStringSet("done", HashSet(set)).apply()
    }

    fun loadStarred(): Set<String> = prefs.getStringSet("starred", emptySet())!!.toSet()

    fun saveStarred(set: Set<String>) {
        prefs.edit().putStringSet("starred", HashSet(set)).apply()
    }
}

/** 搜索：名称、拼音（全拼，忽略空格）、拼音首字母（如 "qxd" → 七星刀） */
fun Fish.matchesQuery(raw: String): Boolean {
    val q = raw.trim().lowercase()
    if (q.isEmpty()) return true
    if (name.contains(q)) return true
    val compact = q.replace(" ", "")
    val joined = pinyin.replace(" ", "")
    if (joined.contains(compact)) return true
    val initials = pinyin.split(" ").filter { it.isNotEmpty() }.joinToString("") { it.take(1) }
    return initials.contains(compact)
}
