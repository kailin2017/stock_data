package com.example.stockskill.model

import com.fasterxml.jackson.annotation.JsonProperty

// --- Raw Responses from Shioaji Server ---

data class ShioajiKBarsRaw(
    @JsonProperty("datetime") val datetime: List<String> = emptyList(),
    @JsonProperty("Open") val open: List<Double> = emptyList(),
    @JsonProperty("High") val high: List<Double> = emptyList(),
    @JsonProperty("Low") val low: List<Double> = emptyList(),
    @JsonProperty("Close") val close: List<Double> = emptyList(),
    @JsonProperty("Volume") val volume: List<Int> = emptyList(),
    @JsonProperty("Amount") val amount: List<Double> = emptyList()
)

data class ShioajiSnapshotRaw(
    val code: String,
    val close: Double = 0.0,
    val volume: Int = 0,
    @JsonProperty("total_volume") val totalVolume: Int = 0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val open: Double = 0.0,
    val tick_type: String = "",
    @JsonProperty("average_price") val averagePrice: Double = 0.0
)

data class ContractRequest(
    val code: String,
    val exchange: String = "TSE",
    @JsonProperty("security_type") val securityType: String = "STK"
)

data class ShioajiSnapshotRequest(
    val contracts: List<ContractRequest>
)

data class ShioajiKBarsRequest(
    val contract: ContractRequest,
    val start: String,
    val end: String
)

// --- Domain Models Exposed to the Skill / Clients ---

data class KBarItem(
    val datetime: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Int,
    val amount: Double
)

data class PriceVolumeInfo(
    val code: String,
    val close: Double,
    val volume: Int, // Represents the accumulated volume for the day
    val high: Double,
    val low: Double,
    val open: Double,
    val change: Double = 0.0
)
