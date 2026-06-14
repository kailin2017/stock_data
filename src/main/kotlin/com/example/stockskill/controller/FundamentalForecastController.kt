package com.example.stockskill.controller

import com.example.stockskill.model.FundamentalForecastInfo
import com.example.stockskill.service.FundamentalForecastService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/stock")
class FundamentalForecastController(
    private val fundamentalForecastService: FundamentalForecastService
) {
    private val log = LoggerFactory.getLogger(FundamentalForecastController::class.java)

    @GetMapping("/fundamental-forecast")
    fun getFundamentalForecast(@RequestParam code: String): FundamentalForecastInfo {
        log.info("Fetching fundamental forecast for stock: {}", code)
        val result = fundamentalForecastService.getFundamentalForecast(code)
        log.info("Retrieved fundamental forecast for stock: {}, consensus: {}, targetPrice: {}", 
            code, result.analystConsensus, result.brokerTargetPrice)
        return result
    }
}
