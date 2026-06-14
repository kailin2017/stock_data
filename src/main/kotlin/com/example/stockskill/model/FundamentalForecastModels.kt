package com.example.stockskill.model

data class FundamentalForecastInfo(
    val code: String,
    val companyName: String,
    val forwardEPS: Double,
    val brokerTargetPrice: Double,
    val targetPriceHigh: Double,
    val targetPriceLow: Double,
    val analystConsensus: String,
    val industryKeywords: List<String>
)
