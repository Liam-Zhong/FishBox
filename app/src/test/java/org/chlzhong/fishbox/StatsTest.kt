package org.chlzhong.fishbox

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsTest {
    private fun fish(id: String, fight: Int) = Fish(
        id = id, name = id, pinyin = id, initial = 'A', description = "", tag = "",
        rods = emptyList(), gear = null, fight = fight,
        locations = emptyList(), times = emptyList(), weather = emptyList(), seasons = emptyList(),
        prices = listOf(1, 2, 3, 4, 5), rarityNames = null,
        body = Color.Red, fin = Color.Blue, shape = 0,
    )

    @Test fun emptyProgress_hasNoTopCatch() {
        val s = computeStats(listOf(fish("a", 3)), emptySet())
        assertEquals(0, s.qualitiesCaught)
        assertNull(s.top)
    }

    @Test fun topCatch_prefersHigherFightLevel() {
        val list = listOf(fish("a", 2), fish("b", 5))
        val done = setOf(key("a", 4), key("b", 0))
        val top = computeStats(list, done).top!!
        assertEquals("b", top.fish.id)
        assertEquals(0, top.quality)
    }

    @Test fun topCatch_tieBrokenByHigherQuality() {
        val list = listOf(fish("a", 4), fish("b", 4))
        val done = setOf(key("a", 1), key("b", 3), key("b", 0))
        val top = computeStats(list, done).top!!
        assertEquals("b", top.fish.id)
        assertEquals(3, top.quality)
    }

    @Test fun perQualityCounts_andCompleteSpecies() {
        val list = listOf(fish("a", 1), fish("b", 1))
        val done = (0 until QUALITY_COUNT).map { key("a", it) }.toSet() + key("b", 2)
        val s = computeStats(list, done)
        assertEquals(listOf(1, 1, 2, 1, 1), s.perQuality)
        assertEquals(6, s.qualitiesCaught)
        assertEquals(1, s.speciesComplete)
        assertEquals(2, s.speciesAny)
    }
}
