package com.example.stockskill.controller

import com.example.stockskill.model.KBarItem
import com.example.stockskill.model.PriceVolumeInfo
import com.example.stockskill.service.ShioajiService
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/stock")
class StockController(
    private val shioajiService: ShioajiService
) {

    @GetMapping("/kline")
    fun getKLines(
        @RequestParam code: String,
        @RequestParam(required = false) start: String?,
        @RequestParam(required = false) end: String?
    ): List<KBarItem> {
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(1).toString()
        
        val actualStart = start ?: yesterday
        val actualEnd = end ?: today
        
        return shioajiService.getKLines(code, actualStart, actualEnd)
    }

    @GetMapping("/price-volume")
    fun getPriceVolume(
        @RequestParam code: String
    ): PriceVolumeInfo {
        return shioajiService.getPriceVolume(code)
    }
}
