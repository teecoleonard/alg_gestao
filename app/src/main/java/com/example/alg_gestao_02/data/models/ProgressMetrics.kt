package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de dados para métricas de progresso/metas do dashboard
 */
data class ProgressMetrics(
    @SerializedName("contratos_meta")
    val contratosMeta: Int,
    
    @SerializedName("contratos_atual")
    val contratosAtual: Int,
    
    @SerializedName("receita_meta")
    val receitaMeta: Double,
    
    @SerializedName("receita_atual")
    val receitaAtual: Double,
    
    @SerializedName("clientes_meta")
    val clientesMeta: Int,
    
    @SerializedName("clientes_atual")
    val clientesAtual: Int,
    
    @SerializedName("satisfacao_percentual")
    val satisfacaoPercentual: Int
) {
    // Propriedades calculadas para percentuais
    val contratosPercentual: Int
        get() = if (contratosMeta > 0) ((contratosAtual * 100) / contratosMeta) else 0
    
    val receitaPercentual: Int
        get() = if (receitaMeta > 0) ((receitaAtual * 100) / receitaMeta).toInt() else 0
    
    val clientesPercentual: Int
        get() = if (clientesMeta > 0) ((clientesAtual * 100) / clientesMeta) else 0
} 