package com.example.fishcodex

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class SortingTest {
    private fun fish(id: String, initial: Char, fight: Int) = Fish(
        id = id, name = id, pinyin = id, initial = initial, description = "", tag = "",
        rods = emptyList(), gear = null, fight = fight,
        locations = emptyList(), times = emptyList(), weather = emptyList(), seasons = emptyList(),
        prices = listOf(1, 2, 3, 4, 5), rarityNames = null,
        body = Color.Red, fin = Color.Blue, shape = 0,
    )

    // 已按拼音排好序的输入（和 loadFish 的输出一致）
    private val shown = listOf(fish("a1", 'A', 3), fish("b1", 'B', 1), fish("b2", 'B', 3), fish("c1", 'C', 1))

    @Test fun alphabet_groupsByInitial() {
        val s = buildSections(shown, SortMode.ALPHABET, emptyList())
        assertEquals(listOf("A", "B", "C"), s.map { it.key })
        assertEquals(listOf("b1", "b2"), s[1].fish.map { it.id })
    }

    @Test fun fight_groupsAscendingAndKeepsAlphabetOrderInside() {
        val s = buildSections(shown, SortMode.FIGHT, emptyList())
        assertEquals(listOf("1", "3"), s.map { it.key })
        assertEquals("搏斗 Lv.1", s[0].title)
        assertEquals(listOf("b1", "c1"), s[0].fish.map { it.id })
        assertEquals(listOf("a1", "b2"), s[1].fish.map { it.id })
    }

    @Test fun game_followsTabOrder_andSkipsFilteredOutFish() {
        val tabs = listOf(listOf("c1", "a1", "zzz"), listOf("b2"))
        val s = buildSections(shown, SortMode.GAME, tabs)
        assertEquals(listOf("页签 1", "页签 2"), s.map { it.title })
        assertEquals(listOf("c1", "a1"), s[0].fish.map { it.id })
        assertEquals(listOf("b2"), s[1].fish.map { it.id })
        // 没出现在任何页签里的鱼（b1）放在最后的"其他"
        assertEquals(listOf("b1"), s[2].fish.map { it.id })
    }

    @Test fun game_withoutTabData_fallsBackToAlphabet() {
        val s = buildSections(shown, SortMode.GAME, emptyList())
        assertEquals(listOf("A", "B", "C"), s.map { it.key })
    }
}
