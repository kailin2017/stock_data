package com.example.stockskill.service

import com.example.stockskill.model.KBarItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RiskSentimentServiceTest {

    private class FakeShioajiService : ShioajiService("http://fake") {
        override fun getKLines(code: String, start: String, end: String): List<KBarItem> {
            return listOf(
                KBarItem("2026-06-11", 100.0, 105.0, 95.0, 100.0, 1000, 100000.0),
                KBarItem("2026-06-12", 100.0, 105.0, 95.0, 102.0, 1000, 102000.0),
                KBarItem("2026-06-13", 102.0, 108.0, 101.0, 105.0, 1200, 126000.0)
            )
        }
    }

    private val shioajiService = FakeShioajiService()
    private val technicalDepthService = TechnicalDepthService(shioajiService)
    private val riskSentimentService = RiskSentimentService(shioajiService, technicalDepthService)

    @Test
    fun testGetRiskSentiment() {
        val result = riskSentimentService.getRiskSentiment("2330")
        assertNotNull(result)
        assertEquals("2330", result.code)
        assertNotNull(result.historicalVolatility)
        assertNotNull(result.fearGreedIndex)
        assertEquals(3, result.events.size)
    }
}
