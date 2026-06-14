package com.example.stockskill.controller

import com.example.stockskill.model.TechnicalDepthInfo
import com.example.stockskill.service.TechnicalDepthService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/stock")
class TechnicalDepthController(
    private val technicalDepthService: TechnicalDepthService
) {
    private val log = LoggerFactory.getLogger(TechnicalDepthController::class.java)

    @GetMapping("/technical-depth")
    fun getTechnicalDepth(
        @RequestParam code: String,
        @RequestParam(defaultValue = "Daily") timeframe: String,
        @RequestParam(required = false) start: String?,
        @RequestParam(required = false) end: String?
    ): TechnicalDepthInfo {
        log.info("Fetching technical depth for stock: {}, timeframe: {}, start: {}, end: {}", 
            code, timeframe, start, end)
        val result = technicalDepthService.getTechnicalDepth(code, timeframe, start, end)
        log.info("Retrieved technical depth for stock: {}, timeframe: {}, kLines size: {}", 
            code, timeframe, result.kLines.size)
        return result
    }
}
