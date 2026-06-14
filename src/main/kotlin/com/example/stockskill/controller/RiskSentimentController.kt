package com.example.stockskill.controller

import com.example.stockskill.model.RiskSentimentInfo
import com.example.stockskill.service.RiskSentimentService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/stock")
class RiskSentimentController(
    private val riskSentimentService: RiskSentimentService
) {
    private val log = LoggerFactory.getLogger(RiskSentimentController::class.java)

    @GetMapping("/risk-sentiment")
    fun getRiskSentiment(@RequestParam code: String): RiskSentimentInfo {
        log.info("Fetching risk sentiment for stock: {}", code)
        val result = riskSentimentService.getRiskSentiment(code)
        log.info("Retrieved risk sentiment for stock: {}, fearGreedIndex: {} ({}), historicalVolatility: {}", 
            code, result.fearGreedIndex, result.fearGreedStatus, result.historicalVolatility)
        return result
    }
}
