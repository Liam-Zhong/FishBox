package org.chlzhong.fishbox

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupTest {
    private val ids = setOf("seven_stars", "shimmerfin")

    @Test fun roundTrip_keepsProgressAndStars() {
        val text = Backup.encode(setOf("seven_stars:0", "shimmerfin:4"), setOf("shimmerfin"), 123L)
        val r = Backup.decode(text, ids).getOrThrow()
        assertEquals(setOf("seven_stars:0", "shimmerfin:4"), r.data.progress)
        assertEquals(setOf("shimmerfin"), r.data.starred)
        assertEquals(0, r.ignored)
    }

    @Test fun unknownFishAndBadKeys_areIgnoredAndCounted() {
        val text = Backup.encode(setOf("seven_stars:1", "ghost_fish:2", "seven_stars:9", "garbage"), setOf("nobody"), 1L)
        val r = Backup.decode(text, ids).getOrThrow()
        assertEquals(setOf("seven_stars:1"), r.data.progress)
        assertTrue(r.data.starred.isEmpty())
        assertEquals(4, r.ignored)
    }

    @Test fun wrongFormat_isRejected() {
        assertTrue(Backup.decode("""{"format":"other","version":1}""", ids).isFailure)
        assertTrue(Backup.decode("not json", ids).isFailure)
    }

    @Test fun newerVersion_isRejected() {
        assertTrue(Backup.decode("""{"format":"fishbox-backup","version":99}""", ids).isFailure)
    }
}
