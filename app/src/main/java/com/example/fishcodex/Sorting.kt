package com.example.fishcodex

/** 列表的一个分组：key 用于右侧索引（字母/数字），title 是分组标题 */
data class Section(val key: String, val title: String, val fish: List<Fish>)

/**
 * 把（已筛选的）鱼按排序方式分组。传入的 shown 已按拼音排好序，所以每个分组内部都保持拼音顺序。
 * 游戏内顺序：按 gameTabs 给出的页签与顺序排列；没出现在页签里的鱼放在最后的"其他"。
 */
fun buildSections(
    shown: List<Fish>,
    mode: SortMode,
    gameTabs: List<List<String>>,
    fightDescending: Boolean = false,
): List<Section> = when (mode) {
    SortMode.ALPHABET -> alphabetSections(shown)
    SortMode.FIGHT -> shown.groupBy { it.fight }
        .toSortedMap(if (fightDescending) compareByDescending<Int> { it } else compareBy<Int> { it })
        .map { (level, list) -> Section(level.toString(), "搏斗 Lv.$level", list) }
    SortMode.GAME -> if (gameTabs.isEmpty()) alphabetSections(shown) else gameSections(shown, gameTabs)
}

private fun alphabetSections(shown: List<Fish>): List<Section> =
    shown.groupBy { it.initial }.toSortedMap().map { (c, list) -> Section(c.toString(), c.toString(), list) }

private fun gameSections(shown: List<Fish>, gameTabs: List<List<String>>): List<Section> {
    val byId = shown.associateBy { it.id }
    val used = HashSet<String>()
    val out = ArrayList<Section>()
    gameTabs.forEachIndexed { i, ids ->
        val list = ids.mapNotNull { byId[it] }
        list.forEach { used += it.id }
        if (list.isNotEmpty()) out += Section((i + 1).toString(), "页签 ${i + 1}", list)
    }
    val rest = shown.filter { it.id !in used }
    if (rest.isNotEmpty()) out += Section("·", "其他", rest)
    return out
}
