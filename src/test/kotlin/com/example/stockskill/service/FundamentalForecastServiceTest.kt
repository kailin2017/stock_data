package com.example.stockskill.service

import com.example.stockskill.model.PriceVolumeInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class FundamentalForecastServiceTest {

    private class FakeShioajiService : ShioajiService("http://fake") {
        override fun getPriceVolume(code: String): PriceVolumeInfo {
            return PriceVolumeInfo(
                code = code,
                close = 150.0,
                volume = 10000,
                high = 152.0,
                low = 148.0,
                open = 150.0,
                change = 0.0
            )
        }
    }

    private val shioajiService = FakeShioajiService()
    private val fundamentalForecastService = FundamentalForecastService(shioajiService)

    @Test
    fun testGetFundamentalForecastTSMC() {
        val result = fundamentalForecastService.getFundamentalForecast("2330")
        assertEquals("2330", result.code)
        assertEquals("Taiwan Semiconductor Manufacturing Company (TSMC)", result.companyName)
        assert(result.industryKeywords.contains("AI"))
    }

    @Test
    fun testGetFundamentalForecastGeneric() {
        val result = fundamentalForecastService.getFundamentalForecast("9999")
        assertEquals("9999", result.code)
        assertEquals("Company 9999", result.companyName)
        assertNotNull(result.brokerTargetPrice)
    }
}
