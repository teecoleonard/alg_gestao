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
 * Modelo de dados para o período de filtro
 */
data class PeriodoFiltro(
    @SerializedName("mes")
    val mes: Int,
    
    @SerializedName("ano")
    val ano: Int
) {
    /**
     * Retorna o período formatado como "Janeiro/2024"
     */
    fun getFormatado(): String {
        val nomesMeses = arrayOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        
        return if (mes in 1..12) {
            "${nomesMeses[mes - 1]}/$ano"
        } else {
            "$mes/$ano"
        }
    }
    
    /**
     * Retorna o período formatado de forma abreviada como "Jan/24"
     */
    fun getFormatadoAbreviado(): String {
        val nomesMesesAbrev = arrayOf(
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
            "Jul", "Ago", "Set", "Out", "Nov", "Dez"
        )
        
        return if (mes in 1..12) {
            "${nomesMesesAbrev[mes - 1]}/${ano.toString().takeLast(2)}"
        } else {
            "$mes/${ano.toString().takeLast(2)}"
        }
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
    val totalClientes: Int,
    
    @SerializedName("periodo")
    val periodo: PeriodoFiltro? = null
) {
    /**
     * Verifica se os dados estão filtrados por um período específico
     */
    fun isFiltradoPorPeriodo(): Boolean {
        return periodo != null
    }
    
    /**
     * Retorna o texto do período para exibição
     */
    fun getTextoPeriodo(): String {
        return if (periodo != null) {
            "Receita de ${periodo.getFormatado()}"
        } else {
            "Receita Total"
        }
    }
} 