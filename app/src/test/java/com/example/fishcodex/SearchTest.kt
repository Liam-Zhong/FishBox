package com.example.fishcodex

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {
    private val fish = Fish(
        id = "seven_stars", name = "七星刀", pinyin = "qi xing dao", initial = 'Q', description = "", tag = "",
        rods = emptyList(), gear = null, fight = 1,
        locations = emptyList(), times = emptyList(), weather = emptyList(), seasons = emptyList(),
        prices = listOf(1, 2, 3, 4, 5), rarityNames = null,
        body = Color.Red, fin = Color.Blue, shape = 0,
    )

    @Test fun matchesChineseName() = assertTrue(fish.matchesQuery("七星"))
    @Test fun matchesFullPinyinIgnoringSpaces() {
        assertTrue(fish.matchesQuery("qixing"))
        assertTrue(fish.matchesQuery("qi xing"))
        assertTrue(fish.matchesQuery("QIXING"))
    }
    @Test fun matchesPinyinInitials() = assertTrue(fish.matchesQuery("qxd"))
    @Test fun blankQueryMatchesEverything() = assertTrue(fish.matchesQuery("   "))
    @Test fun nonMatchingQuery() = assertFalse(fish.matchesQuery("金蛙"))
}
