package org.chlzhong.fishbox

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UiIconTest {
    @Test fun picksWebpThenPng_andNullWhenMissing() {
        val files = setOf("weather_sunny.png", "time_dawn.webp", "time_dawn.png")
        assertEquals("weather_sunny.png", pickUiIconFile(files, "weather", "sunny"))
        assertEquals("time_dawn.webp", pickUiIconFile(files, "time", "dawn"))
        assertNull(pickUiIconFile(files, "season", "spring"))
    }
}
