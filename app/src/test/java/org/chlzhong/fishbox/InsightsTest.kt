package org.chlzhong.fishbox

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class InsightsTest {
    private fun fish(id: String, seasons: List<String>, locations: List<String>) = Fish(
        id = id, name = id, pinyin = id, initial = 'A', description = "", tag = "",
        rods = emptyList(), gear = null, fight = 1,
        locations = locations, times = emptyList(), weather = emptyList(), seasons = seasons,
        prices = listOf(1, 2, 3, 4, 5), rarityNames = null,
        body = Color.Red, fin = Color.Blue, shape = 0,
    )

    @Test fun nearComplete_listsFishMissingExactlyOneQuality() {
        val list = listOf(fish("a", listOf("spring"), listOf("pond")), fish("b", listOf("spring"), listOf("pond")))
        val done = (0 until 4).map { key("a", it) }.toSet() + key("b", 0)
        val near = computeInsights(list, done).nearComplete
        assertEquals(1, near.size)
        assertEquals("a", near[0].fish.id)
        assertEquals(4, near[0].missingQuality)
    }

    @Test fun groupProgress_countsQualitiesPerSeasonAndLocation() {
        val list = listOf(
            fish("a", listOf("spring", "winter"), listOf("pond")),
            fish("b", listOf("winter"), listOf("dock", "pond")),
        )
        val done = setOf(key("a", 0), key("a", 1), key("b", 0))
        val ins = computeInsights(list, done)
        val winter = ins.bySeason.first { it.id == "winter" }
        assertEquals(3, winter.collected)
        assertEquals(10, winter.total)
        val dock = ins.byLocation.first { it.id == "dock" }
        assertEquals(1, dock.collected)
        assertEquals(5, dock.total)
        assertEquals(null, ins.bySeason.firstOrNull { it.id == "summer" }) // 没有鱼的季节不出现
    }
}
