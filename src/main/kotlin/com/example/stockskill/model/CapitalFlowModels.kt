package com.example.stockskill.model

data class CapitalFlowInfo(
    val code: String,
    val date: String,
    val netForeignBuy: Long,
    val netInvestmentTrustBuy: Long,
    val netDealerBuy: Long,
    val whaleNetFlow: Long,
    val retailNetFlow: Long,
    val capitalConcentrationRatio: Double,
    val institutionalOwnershipPct: Double
)
