package com.example.fishcodex

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class RodDisplayTest {
    private fun fish(kinds: Set<String>, usable: List<Rod>) = Fish(
        id = "x", name = "x", pinyin = "x", initial = 'X', description = "", tag = "",
        rods = emptyList(), gear = null, fight = 1,
        locations = emptyList(), times = emptyList(), weather = emptyList(), seasons = emptyList(),
        prices = listOf(1, 2, 3, 4, 5), rarityNames = null,
        rodKinds = kinds, usableRods = usable,
        body = Color.Red, fin = Color.Blue, shape = 0,
    )

    private val toyTruck = Rod("toy_truck", "大黄车", "lure", "heavy")
    private val frost = Rod("frost", "冰霜", "lure", "heavy")
    private val stick = Rod("stick", "树杈子", "simple", "light")

    @Test fun singleUsableRod_showsThatRodNotTheKind() =
        assertEquals("大黄车", fish(setOf("lure"), listOf(toyTruck)).rodDisplay())

    @Test fun severalRods_showKindsFromLocation() =
        assertEquals("路亚", fish(setOf("lure"), listOf(frost, toyTruck)).rodDisplay())

    @Test fun simpleRodAdds简易ToTheKinds() =
        assertEquals("路亚/简易", fish(setOf("lure"), listOf(Rod("icicle", "冰凌", "lure", "light"), stick)).rodDisplay())

    @Test fun noRodData_fallsBackToKinds() =
        assertEquals("池钓", fish(setOf("pond"), emptyList()).rodDisplay())
}
