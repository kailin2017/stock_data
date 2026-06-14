package com.example.stockskill.model

data class TechnicalIndicatorItem(
    val datetime: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Int,
    val amount: Double,
    val rsi: Double?,
    val macd: Double?,
    val macdSignal: Double?,
    val macdHist: Double?,
    val bbUpper: Double?,
    val bbMiddle: Double?,
    val bbLower: Double?
)

data class VolumeProfileBin(
    val priceRangeStart: Double,
    val priceRangeEnd: Double,
    val volume: Long
)

data class TechnicalDepthInfo(
    val code: String,
    val timeframe: String,
    val kLines: List<TechnicalIndicatorItem>,
    val volumeProfile: List<VolumeProfileBin>
)
