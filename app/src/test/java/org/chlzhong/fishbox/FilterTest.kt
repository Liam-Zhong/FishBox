package org.chlzhong.fishbox

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterTest {
    private fun fish(
        id: String,
        seasons: List<String> = emptyList(),
        times: List<String> = emptyList(),
        weather: List<String> = emptyList(),
        locations: List<String> = emptyList(),
        rods: List<String> = emptyList(),
        rodKinds: Set<String> = emptySet(),
        gear: String? = null,
    ) = Fish(
        id = id, name = id, pinyin = id, initial = 'A', description = "", tag = "",
        rods = rods, gear = gear, fight = 1,
        locations = locations, times = times, weather = weather, seasons = seasons,
        prices = listOf(1, 2, 3, 4, 5), rarityNames = null, rodKinds = rodKinds,
        body = Color.Red, fin = Color.Blue, shape = 0,
    )

    private val list = listOf(
        fish("river", seasons = listOf("spring"), locations = listOf("riverside", "dock"), rods = listOf("lure", "pond")),
        fish("pondfish", seasons = listOf("spring", "winter"), locations = listOf("pond"), rods = listOf("pond")),
        fish("ice", seasons = listOf("winter"), locations = listOf("ice_hole"), times = listOf("dawn"), weather = listOf("sunny")),
    )

    private fun ids(f: Filters) = list.filter { f.matches(it, emptySet()) }.map { it.id }

    @Test fun noFilters_matchesEverything() = assertEquals(listOf("river", "pondfish", "ice"), ids(Filters()))

    @Test fun location_isOrWithinCategory() =
        assertEquals(listOf("river", "ice"), ids(Filters(locations = setOf("dock", "ice_hole"))))

    @Test fun categories_areAnded() =
        assertEquals(listOf("pondfish"), ids(Filters(seasons = setOf("spring"), locations = setOf("pond"))))

    @Test fun timeAndWeather_filter() =
        assertEquals(listOf("ice"), ids(Filters(times = setOf("dawn"), weathers = setOf("sunny"))))

    @Test fun rod_usesLocationDerivedKinds_notRawRods() {
        // 站点原始 rods 里带 pond 的河流鱼（如芭蕾蓬蓬鱼），按地点推导后只有 lure
        val ballet = fish("ballet", locations = listOf("riverside"), rods = listOf("lure", "pond"), rodKinds = setOf("lure"))
        val pondFish = fish("pondfish2", locations = listOf("pond"), rods = listOf("pond"), rodKinds = setOf("pond"))
        val both = fish("both", locations = listOf("pond", "dock"), rodKinds = setOf("pond", "lure"))
        val all = listOf(ballet, pondFish, both)
        assertEquals(listOf("pondfish2", "both"), all.filter { Filters(rods = setOf("pond")).matches(it, emptySet()) }.map { it.id })
        assertEquals(listOf("ballet", "both"), all.filter { Filters(rods = setOf("lure")).matches(it, emptySet()) }.map { it.id })
        assertEquals(1, Filters(rods = setOf("pond")).activeCount)
    }

    @Test fun gear_filter_matchesLightOrHeavy() {
        val all = listOf(fish("l", gear = "light"), fish("h", gear = "heavy"), fish("n", gear = null))
        assertEquals(listOf("l"), all.filter { Filters(gears = setOf("light")).matches(it, emptySet()) }.map { it.id })
        assertEquals(listOf("l", "h"), all.filter { Filters(gears = setOf("light", "heavy")).matches(it, emptySet()) }.map { it.id })
        assertEquals(1, Filters(gears = setOf("heavy")).activeCount)
    }

    @Test fun status_starred_usesFollowList() {
        assertEquals(listOf("ice"), list.filter { Filters(status = 3).matches(it, emptySet(), setOf("ice")) }.map { it.id })
    }

    @Test fun status_usesCollectionProgress() {
        val done = (0 until QUALITY_COUNT).map { key("river", it) }.toSet()
        assertEquals(listOf("river"), list.filter { Filters(status = 1).matches(it, done) }.map { it.id })
        assertEquals(listOf("pondfish", "ice"), list.filter { Filters(status = 2).matches(it, done) }.map { it.id })
    }
}
