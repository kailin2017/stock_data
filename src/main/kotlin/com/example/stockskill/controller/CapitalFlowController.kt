package com.example.stockskill.controller

import com.example.stockskill.model.CapitalFlowInfo
import com.example.stockskill.service.CapitalFlowService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/stock")
class CapitalFlowController(
    private val capitalFlowService: CapitalFlowService
) {
    private val log = LoggerFactory.getLogger(CapitalFlowController::class.java)

    @GetMapping("/capital-flow")
    fun getCapitalFlow(@RequestParam code: String): CapitalFlowInfo {
        log.info("Fetching capital flow for stock: {}", code)
        val result = capitalFlowService.getCapitalFlow(code)
        log.info("Retrieved capital flow for stock: {}, date: {}, institutionalOwnership: {}%", 
            code, result.date, result.institutionalOwnershipPct)
        return result
    }
}
