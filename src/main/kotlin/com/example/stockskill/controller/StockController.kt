package com.example.stockskill.controller

import com.example.stockskill.model.KBarItem
import com.example.stockskill.model.PriceVolumeInfo
import com.example.stockskill.service.ShioajiService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/stock")
class StockController(
    private val shioajiService: ShioajiService
) {
    private val log = LoggerFactory.getLogger(StockController::class.java)

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
        
        log.info("Fetching K-lines for stock: {}, start: {}, end: {} (requested start: {}, end: {})", 
            code, actualStart, actualEnd, start, end)
        val result = shioajiService.getKLines(code, actualStart, actualEnd)
        log.info("Retrieved {} K-line items for stock: {}", result.size, code)
        return result
    }

    @GetMapping("/price-volume")
    fun getPriceVolume(
        @RequestParam code: String
    ): PriceVolumeInfo {
        log.info("Fetching price-volume snapshot for stock: {}", code)
        val result = shioajiService.getPriceVolume(code)
        log.info("Retrieved price-volume snapshot for stock: {}, close: {}, volume: {}", 
            code, result.close, result.volume)
        return result
    }
}
