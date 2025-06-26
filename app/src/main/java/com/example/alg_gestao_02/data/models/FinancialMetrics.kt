package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de dados para métricas financeiras do dashboard
 */
data class FinancialMetrics(
    @SerializedName("valor_total_ativo")
    val valorTotalAtivo: Double,
    
    @SerializedName("receita_mensal")
    val receitaMensal: Double,
    
    @SerializedName("ticket_medio")
    val ticketMedio: Double
) 