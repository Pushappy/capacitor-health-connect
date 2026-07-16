package com.ubiehealth.capacitor.healthconnect

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class SerializerTest {

    @Test
    fun `getLocalDateTime converts a CET winter instant to Stockholm wall-clock time`() {
        val obj = JSONObject().put("time", "2024-01-15T23:30:00Z")

        val result = obj.getLocalDateTime("time", ZoneId.of("Europe/Stockholm"))

        assertEquals(LocalDateTime.of(2024, 1, 16, 0, 30), result)
    }

    @Test
    fun `getLocalDateTime converts a CEST summer instant to Stockholm wall-clock time`() {
        val obj = JSONObject().put("time", "2024-07-15T22:30:00Z")

        val result = obj.getLocalDateTime("time", ZoneId.of("Europe/Stockholm"))

        assertEquals(LocalDateTime.of(2024, 7, 16, 0, 30), result)
    }

    @Test
    fun `getLocalDateTime produces different wall-clock digits for different zoneIds`() {
        val obj = JSONObject().put("time", "2024-07-15T22:30:00Z")

        val stockholm = obj.getLocalDateTime("time", ZoneId.of("Europe/Stockholm"))
        val honolulu = obj.getLocalDateTime("time", ZoneId.of("Pacific/Honolulu"))

        assertNotEquals(stockholm, honolulu)
    }

    @Test
    fun `getLocalTimeRangeFilter resolves a different filter per zoneId for the same instants`() {
        fun filterWithZone(zoneId: String) = JSONObject()
            .put(
                "timeRangeFilter",
                JSONObject()
                    .put("type", "between")
                    .put("localStartTime", "2024-01-15T23:00:00Z")
                    .put("localEndTime", "2024-01-16T23:00:00Z")
                    .put("zoneId", zoneId),
            )
            .getLocalTimeRangeFilter("timeRangeFilter")

        val stockholm = filterWithZone("Europe/Stockholm")
        val honolulu = filterWithZone("Pacific/Honolulu")

        // Same instants, different zoneId - the resolved local-time boundaries must differ.
        assertNotEquals(stockholm.toString(), honolulu.toString())
    }
}
