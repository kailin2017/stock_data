package com.example.stockskill.service

import com.example.stockskill.model.PriceVolumeInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class CapitalFlowServiceTest {

    private class FakeShioajiService : ShioajiService("http://fake") {
        override fun getPriceVolume(code: String): PriceVolumeInfo {
            return PriceVolumeInfo(
                code = code,
                close = 1000.0,
                volume = 50000,
                high = 1010.0,
                low = 990.0,
                open = 1000.0,
                change = 0.0
            )
        }
    }

    private val shioajiService = FakeShioajiService()
    private val capitalFlowService = CapitalFlowService(shioajiService)

    @Test
    fun testGetCapitalFlow() {
        val result = capitalFlowService.getCapitalFlow("2330")
        assertNotNull(result)
        assertEquals("2330", result.code)
        assert(result.capitalConcentrationRatio in 0.4..0.85)
        assert(result.institutionalOwnershipPct in 10.0..75.0)
    }
}
