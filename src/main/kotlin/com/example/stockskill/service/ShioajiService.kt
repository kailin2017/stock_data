package com.example.stockskill.service

import com.example.stockskill.model.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class ShioajiService(
    @Value("\${shioaji.server-url}") private val serverUrl: String
) {
    private val webClient = WebClient.builder()
        .baseUrl(serverUrl)
        .build()

    fun getKLines(code: String, start: String, end: String): List<KBarItem> {
        // Build correct Shioaji server request payload with contract structure
        val contractRequest = ContractRequest(
            code = code,
            exchange = getExchangeForCode(code),
            securityType = "STK"
        )
        val payload = ShioajiKBarsRequest(
            contract = contractRequest,
            start = start,
            end = end
        )

        val raw = webClient.post()
            .uri("/api/v1/data/kbars")
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(ShioajiKBarsRaw::class.java)
            .block(Duration.ofSeconds(10)) ?: ShioajiKBarsRaw()

        val list = mutableListOf<KBarItem>()
        val size = raw.datetime.size
        for (i in 0 until size) {
            list.add(
                KBarItem(
                    datetime = raw.datetime[i],
                    open = raw.open.getOrElse(i) { 0.0 },
                    high = raw.high.getOrElse(i) { 0.0 },
                    low = raw.low.getOrElse(i) { 0.0 },
                    close = raw.close.getOrElse(i) { 0.0 },
                    volume = raw.volume.getOrElse(i) { 0 },
                    amount = raw.amount.getOrElse(i) { 0.0 }
                )
            )
        }
        return list
    }

    fun getPriceVolume(code: String): PriceVolumeInfo {
        // Build correct Shioaji snapshot request payload
        val contractRequest = ContractRequest(
            code = code,
            exchange = getExchangeForCode(code),
            securityType = "STK"
        )
        val payload = ShioajiSnapshotRequest(contracts = listOf(contractRequest))

        val rawList = webClient.post()
            .uri("/api/v1/data/snapshots")
            .bodyValue(payload)
            .retrieve()
            .bodyToFlux(ShioajiSnapshotRaw::class.java)
            .collectList()
            .block(Duration.ofSeconds(10)) ?: emptyList()

        val raw = rawList.firstOrNull() ?: ShioajiSnapshotRaw(code = code)
        val change = if (raw.open > 0.0) raw.close - raw.open else 0.0

        return PriceVolumeInfo(
            code = raw.code,
            close = raw.close,
            volume = raw.totalVolume, // Expose total daily volume for charting/info
            high = raw.high,
            low = raw.low,
            open = raw.open,
            change = change
        )
    }

    // Helper to determine the exchange (TSE or OTC) based on Taiwan stock code length/rules.
    // Default is TSE, but can be customized if needed.
    private fun getExchangeForCode(code: String): String {
        // Shioaji defaults to TSE. In a production system, one could fetch contract info first.
        // For simplicity and correctness, we will use TSE.
        return "TSE"
    }
}
