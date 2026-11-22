package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import ru.ngieu.raspisanie.data.AppUpdateParser

class AppUpdateParserTest {

    @Test
    fun `returns update info when remote release build is newer`() {
        val raw = """
            {
              "tag_name": "v1.0.25",
              "name": "v1.0.25",
              "body": "UI polish and in-app updates",
              "assets": [
                {
                  "name": "ngieu-c25io-schedule-v1.0.25.apk",
                  "browser_download_url": "https://github.com/xisscag-ops/raspisanie/releases/download/v1.0.25/ngieu-c25io-schedule-v1.0.25.apk",
                  "size": 1800000
                }
              ]
            }
        """.trimIndent()

        val update = AppUpdateParser.parseRelease(
            rawJson = raw,
            currentVersionCode = 24,
            currentVersionName = "1.0.24",
        )

        assertNotNull(update)
        assertEquals(25, update?.versionCode)
        assertEquals("1.0.25", update?.versionName)
        assertEquals(1800000L, update?.sizeBytes)
        assertEquals("UI polish and in-app updates", update?.releaseNotes)
    }

    @Test
    fun `returns null when remote release is same or older`() {
        val raw = """
            {
              "tag_name": "v1.0.24",
              "assets": [
                {
                  "name": "ngieu-c25io-schedule-v1.0.24.apk",
                  "browser_download_url": "https://github.com/xisscag-ops/raspisanie/releases/download/v1.0.24/ngieu-c25io-schedule-v1.0.24.apk",
                  "size": 1762351
                }
              ]
            }
        """.trimIndent()

        val same = AppUpdateParser.parseRelease(
            rawJson = raw,
            currentVersionCode = 24,
            currentVersionName = "1.0.24",
        )
        assertNull(same)
    }
}
