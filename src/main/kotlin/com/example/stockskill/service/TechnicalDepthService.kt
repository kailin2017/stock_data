package com.example.stockskill.service

import com.example.stockskill.model.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TechnicalDepthService(
    private val shioajiService: ShioajiService
) {
    private val log = LoggerFactory.getLogger(TechnicalDepthService::class.java)

    fun getTechnicalDepth(
        code: String,
        timeframe: String,
        start: String?,
        end: String?
    ): TechnicalDepthInfo {
        val today = LocalDate.now().toString()
        val yesterday = LocalDate.now().minusDays(3).toString() // use 3 days to get enough data for indicators
        val actualStart = start ?: yesterday
        val actualEnd = end ?: today
        log.info("Calculating technical depth for stock code: {}, timeframe: {}, range: {} to {}", 
            code, timeframe, actualStart, actualEnd)

        // 1. Fetch raw daily K-lines
        val dailyKLines = shioajiService.getKLines(code, actualStart, actualEnd)
        if (dailyKLines.isEmpty()) {
            log.warn("No K-lines found for stock code: {} in range: {} to {}", code, actualStart, actualEnd)
            return TechnicalDepthInfo(
                code = code,
                timeframe = timeframe,
                kLines = emptyList(),
                volumeProfile = emptyList()
            )
        }
        log.debug("Fetched {} raw daily K-lines for stock: {}", dailyKLines.size, code)

        // 2. Interpolate based on timeframe
        val interpolatedKLines = mutableListOf<KBarItem>()
        for (bar in dailyKLines) {
            interpolatedKLines.addAll(interpolateBar(bar, timeframe))
        }
        log.debug("Interpolated K-lines count: {} for timeframe: {}", interpolatedKLines.size, timeframe)

        // 3. Extract close prices for indicators
        val closes = interpolatedKLines.map { it.close }

        // 4. Calculate indicators
        log.debug("Calculating indicators (RSI, MACD, Bollinger Bands) for stock: {}", code)
        val rsiList = calculateRSI(closes)
        val (macdLine, macdSignal, macdHist) = calculateMACD(closes)
        val (bbUpper, bbMiddle, bbLower) = calculateBollingerBands(closes)

        // 5. Combine into TechnicalIndicatorItem list
        val indicatorItems = interpolatedKLines.mapIndexed { index, kBar ->
            TechnicalIndicatorItem(
                datetime = kBar.datetime,
                open = kBar.open,
                high = kBar.high,
                low = kBar.low,
                close = kBar.close,
                volume = kBar.volume,
                amount = kBar.amount,
                rsi = rsiList.getOrNull(index),
                macd = macdLine.getOrNull(index),
                macdSignal = macdSignal.getOrNull(index),
                macdHist = macdHist.getOrNull(index),
                bbUpper = bbUpper.getOrNull(index),
                bbMiddle = bbMiddle.getOrNull(index),
                bbLower = bbLower.getOrNull(index)
            )
        }

        // 6. Calculate Volume Profile
        log.debug("Calculating volume profile for stock: {}", code)
        val volumeProfile = calculateVolumeProfile(interpolatedKLines)

        val result = TechnicalDepthInfo(
            code = code,
            timeframe = timeframe,
            kLines = indicatorItems,
            volumeProfile = volumeProfile
        )
        log.info("Finished technical depth calculation for stock: {}. Indicator items count: {}, Volume Profile bins: {}", 
            code, result.kLines.size, result.volumeProfile.size)
        return result
    }

    private fun interpolateBar(parent: KBarItem, timeframe: String): List<KBarItem> {
        log.trace("Interpolating bar at {} for timeframe: {}", parent.datetime, timeframe)
        val numBars = when (timeframe.lowercase()) {
            "5m" -> 54
            "15m" -> 18
            "1h" -> 5
            else -> 1
        }

        if (numBars <= 1) return listOf(parent)

        val timeStepMinutes = when (timeframe.lowercase()) {
            "5m" -> 5
            "15m" -> 15
            "1h" -> 60
            else -> 60
        }

        val dateStr = if (parent.datetime.contains(" ")) parent.datetime.substringBefore(" ") else parent.datetime
        val result = mutableListOf<KBarItem>()
        val r = java.util.Random(parent.datetime.hashCode().toLong())

        val baseVolumePerBar = parent.volume / numBars
        val baseAmountPerBar = parent.amount / numBars
        var currentClose = parent.open

        for (i in 0 until numBars) {
            val nextOpen = currentClose
            val targetClose = if (i == numBars - 1) {
                parent.close
            } else {
                val dist = parent.close - nextOpen
                val step = dist / (numBars - i) + (r.nextGaussian() * (parent.high - parent.low) / (2.0 * numBars))
                val rawClose = nextOpen + step
                rawClose.coerceIn(parent.low, parent.high)
            }

            val barLow = minOf(nextOpen, targetClose) - (r.nextDouble() * (parent.high - parent.low) / (4.0 * numBars))
            val barHigh = maxOf(nextOpen, targetClose) + (r.nextDouble() * (parent.high - parent.low) / (4.0 * numBars))

            val coercedLow = barLow.coerceIn(parent.low, parent.high)
            val coercedHigh = barHigh.coerceIn(parent.low, parent.high)

            val hour = 9 + (i * timeStepMinutes) / 60
            val minute = (i * timeStepMinutes) % 60
            val timeStr = String.format("%s %02d:%02d:00", dateStr, hour, minute)

            result.add(
                KBarItem(
                    datetime = timeStr,
                    open = nextOpen,
                    high = maxOf(nextOpen, targetClose, coercedHigh),
                    low = minOf(nextOpen, targetClose, coercedLow),
                    close = targetClose,
                    volume = (baseVolumePerBar + if (r.nextBoolean()) 1 else -1).coerceAtLeast(1),
                    amount = baseAmountPerBar
                )
            )
            currentClose = targetClose
        }
        return result
    }

    fun calculateRSI(closes: List<Double>, period: Int = 14): List<Double?> {
        if (closes.size <= period) return List(closes.size) { null }
        val rsi = MutableList<Double?>(closes.size) { null }

        var avgGain = 0.0
        var avgLoss = 0.0

        for (i in 1..period) {
            val change = closes[i] - closes[i - 1]
            if (change > 0) avgGain += change else avgLoss += -change
        }
        avgGain /= period
        avgLoss /= period

        if (avgLoss == 0.0) {
            rsi[period] = 100.0
        } else {
            val rs = avgGain / avgLoss
            rsi[period] = 100.0 - (100.0 / (1.0 + rs))
        }

        for (i in (period + 1) until closes.size) {
            val change = closes[i] - closes[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) -change else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period

            if (avgLoss == 0.0) {
                rsi[i] = 100.0
            } else {
                val rs = avgGain / avgLoss
                rsi[i] = 100.0 - (100.0 / (1.0 + rs))
            }
        }
        return rsi.map { it?.let { Math.round(it * 100.0) / 100.0 } }
    }

    fun calculateEMA(values: List<Double>, period: Int): List<Double?> {
        if (values.size < period) return List(values.size) { null }
        val ema = MutableList<Double?>(values.size) { null }
        val multiplier = 2.0 / (period + 1)

        var sum = 0.0
        for (i in 0 until period) {
            sum += values[i]
        }
        var currentEma = sum / period
        ema[period - 1] = currentEma

        for (i in period until values.size) {
            currentEma = (values[i] - currentEma) * multiplier + currentEma
            ema[i] = currentEma
        }
        return ema
    }

    fun calculateMACD(closes: List<Double>): Triple<List<Double?>, List<Double?>, List<Double?>> {
        val ema12 = calculateEMA(closes, 12)
        val ema26 = calculateEMA(closes, 26)

        val macdLine = MutableList<Double?>(closes.size) { null }
        for (i in closes.indices) {
            val e12 = ema12[i]
            val e26 = ema26[i]
            if (e12 != null && e26 != null) {
                macdLine[i] = e12 - e26
            }
        }

        val nonNullMacdIndices = macdLine.indices.filter { macdLine[it] != null }
        val nonNullMacdValues = nonNullMacdIndices.map { macdLine[it]!! }
        val signalNonNull = calculateEMA(nonNullMacdValues, 9)

        val signalLine = MutableList<Double?>(closes.size) { null }
        for (i in nonNullMacdIndices.indices) {
            val originalIdx = nonNullMacdIndices[i]
            signalLine[originalIdx] = signalNonNull.getOrNull(i)
        }

        val macdHist = MutableList<Double?>(closes.size) { null }
        for (i in closes.indices) {
            val m = macdLine[i]
            val s = signalLine[i]
            if (m != null && s != null) {
                macdHist[i] = m - s
            }
        }

        val roundHelper = { v: Double? -> v?.let { Math.round(it * 100.0) / 100.0 } }
        return Triple(macdLine.map(roundHelper), signalLine.map(roundHelper), macdHist.map(roundHelper))
    }

    fun calculateBollingerBands(
        closes: List<Double>,
        period: Int = 20,
        multiplier: Double = 2.0
    ): Triple<List<Double?>, List<Double?>, List<Double?>> {
        val upper = MutableList<Double?>(closes.size) { null }
        val middle = MutableList<Double?>(closes.size) { null }
        val lower = MutableList<Double?>(closes.size) { null }

        if (closes.size < period) {
            return Triple(upper, middle, lower)
        }

        for (i in (period - 1) until closes.size) {
            val subList = closes.subList(i - period + 1, i + 1)
            val sma = subList.average()
            val variance = subList.map { (it - sma) * (it - sma) }.sum() / period
            val stdDev = Math.sqrt(variance)

            middle[i] = Math.round(sma * 100.0) / 100.0
            upper[i] = Math.round((sma + multiplier * stdDev) * 100.0) / 100.0
            lower[i] = Math.round((sma - multiplier * stdDev) * 100.0) / 100.0
        }

        return Triple(upper, middle, lower)
    }

    fun calculateVolumeProfile(kLines: List<KBarItem>, numBins: Int = 10): List<VolumeProfileBin> {
        if (kLines.isEmpty()) return emptyList()
        val minPrice = kLines.minOf { it.low }
        val maxPrice = kLines.maxOf { it.high }

        if (maxPrice == minPrice) {
            return listOf(
                VolumeProfileBin(
                    minPrice,
                    maxPrice,
                    kLines.sumOf { it.volume.toLong() }
                )
            )
        }

        val binSize = (maxPrice - minPrice) / numBins
        val bins = List(numBins) { idx ->
            val start = minPrice + idx * binSize
            val end = start + binSize
            VolumeProfileBin(
                Math.round(start * 100.0) / 100.0,
                Math.round(end * 100.0) / 100.0,
                0L
            )
        }

        val mutableBins = bins.toMutableList()
        for (kline in kLines) {
            val price = kline.close
            var binIdx = ((price - minPrice) / binSize).toInt()
            if (binIdx >= numBins) binIdx = numBins - 1
            if (binIdx < 0) binIdx = 0

            val current = mutableBins[binIdx]
            mutableBins[binIdx] = current.copy(volume = current.volume + kline.volume)
        }
        return mutableBins
    }
}
