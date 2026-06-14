package com.example.stockskill.model

data class StockEvent(
    val date: String,
    val title: String,
    val description: String,
    val impact: String
)

data class RiskSentimentInfo(
    val code: String,
    val historicalVolatility: Double,
    val volatilityStatus: String,
    val fearGreedIndex: Int,
    val fearGreedStatus: String,
    val events: List<StockEvent>
)
