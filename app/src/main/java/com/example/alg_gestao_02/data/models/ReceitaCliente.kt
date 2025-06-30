package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de dados para receita mensal por cliente
 */
data class ReceitaCliente(
    @SerializedName("cliente_id")
    val clienteId: Int,
    
    @SerializedName("cliente_nome")
    val clienteNome: String,
    
    @SerializedName("valor_mensal")
    val valorMensal: Double,
    
    @SerializedName("total_contratos")
    val totalContratos: Int,
    
    @SerializedName("contratos_ativos")
    val contratosAtivos: Int,
    
    @SerializedName("ticket_medio")
    val ticketMedio: Double
) {
    /**
     * Retorna o valor mensal formatado como moeda
     */
    fun getValorMensalFormatado(): String {
        val formato = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR"))
        return formato.format(valorMensal)
    }
    
    /**
     * Retorna o ticket médio formatado como moeda
     */
    fun getTicketMedioFormatado(): String {
        val formato = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR"))
        return formato.format(ticketMedio)
    }
    
    /**
     * Calcula a participação percentual em relação ao total
     */
    fun calcularParticipacao(valorTotal: Double): Double {
        return if (valorTotal > 0) (valorMensal / valorTotal) * 100 else 0.0
    }
}

/**
 * Modelo de resposta da API para receita por cliente
 */
data class ReceitaClienteResponse(
    @SerializedName("clientes")
    val clientes: List<ReceitaCliente>,
    
    @SerializedName("total_geral")
    val totalGeral: Double,
    
    @SerializedName("total_clientes")
    val totalClientes: Int
) 