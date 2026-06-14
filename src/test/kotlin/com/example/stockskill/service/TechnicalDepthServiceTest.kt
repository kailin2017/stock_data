package com.example.stockskill.service

import com.example.stockskill.model.KBarItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TechnicalDepthServiceTest {

    private class FakeShioajiService : ShioajiService("http://fake") {
        override fun getKLines(code: String, start: String, end: String): List<KBarItem> {
            return listOf(
                KBarItem("2026-06-12", 100.0, 105.0, 95.0, 102.0, 1000, 102000.0),
                KBarItem("2026-06-13", 102.0, 108.0, 101.0, 106.0, 1200, 127200.0)
            )
        }
    }

    private val shioajiService = FakeShioajiService()
    private val technicalDepthService = TechnicalDepthService(shioajiService)

    @Test
    fun testTechnicalIndicatorsCalculations() {
        val closes = listOf(10.0, 11.0, 12.0, 11.0, 10.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 13.0, 12.0, 11.0, 12.0, 13.0)
        val rsiList = technicalDepthService.calculateRSI(closes, 14)
        assertEquals(closes.size, rsiList.size)
        assertNull(rsiList[0])
        assertNull(rsiList[13])
        assertNotNull(rsiList[14])

        val (upper, middle, lower) = technicalDepthService.calculateBollingerBands(closes, 5)
        assertEquals(closes.size, upper.size)
        assertNull(upper[3])
        assertNotNull(upper[4])

        val (macd, signal, hist) = technicalDepthService.calculateMACD(closes)
        assertEquals(closes.size, macd.size)
    }

    @Test
    fun testGetTechnicalDepthDaily() {
        val result = technicalDepthService.getTechnicalDepth("2330", "Daily", "2026-06-11", "2026-06-13")
        assertEquals("2330", result.code)
        assertEquals("Daily", result.timeframe)
        assertEquals(2, result.kLines.size)
    }

    @Test
    fun testGetTechnicalDepthIntraday() {
        val result = technicalDepthService.getTechnicalDepth("2330", "1H", "2026-06-11", "2026-06-13")
        assertEquals("2330", result.code)
        assertEquals("1H", result.timeframe)
        assertEquals(10, result.kLines.size)
    }
}
