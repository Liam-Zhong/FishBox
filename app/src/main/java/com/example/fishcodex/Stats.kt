package com.example.fishcodex

data class NearFish(val fish: Fish, val missingQuality: Int)

/** 某个季节/地点下的收集进度：collected 个品质 / total 个品质 */
data class GroupProgress(val id: String, val collected: Int, val total: Int)

data class Insights(
    val nearComplete: List<NearFish>,
    val bySeason: List<GroupProgress>,
    val byLocation: List<GroupProgress>,
)

/** 统计面板用的数据：只差一档集齐的鱼，以及按季节、按地点的完成度 */
fun computeInsights(fishList: List<Fish>, done: Set<String>): Insights {
    val near = fishList.mapNotNull { f ->
        val missing = (0 until QUALITY_COUNT).filter { key(f.id, it) !in done }
        if (missing.size == 1) NearFish(f, missing[0]) else null
    }
    fun group(order: List<String>, of: (Fish) -> List<String>): List<GroupProgress> =
        order.map { id ->
            val members = fishList.filter { id in of(it) }
            GroupProgress(id, members.sumOf { collectedCount(it, done) }, members.size * QUALITY_COUNT)
        }.filter { it.total > 0 }
    return Insights(near, group(SEASON_ORDER) { it.seasons }, group(LOC_ORDER) { it.locations })
}
