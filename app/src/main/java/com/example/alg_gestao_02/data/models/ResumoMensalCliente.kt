package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modelo expandido para resumo mensal detalhado do cliente
 */
data class ResumoMensalCliente(
    @SerializedName("cliente_id")
    val clienteId: Int,
    
    @SerializedName("cliente_nome")
    val clienteNome: String,
    
    @SerializedName("mes_referencia")
    val mesReferencia: String, // formato: "2024-01"
    
    @SerializedName("valor_mensal")
    val valorMensal: Double,
    
    @SerializedName("total_contratos")
    val totalContratos: Int,
    
    @SerializedName("contratos_ativos")
    val contratosAtivos: Int,
    
    @SerializedName("contratos_mes")
    val contratosMes: Int, // Contratos que entraram no mês
    
    @SerializedName("devolucoes_mes")
    val devolucoesMes: Int, // Devoluções no mês
    
    @SerializedName("valor_devolucoes")
    val valorDevolucoes: Double, // Valor das devoluções (multas, etc.)
    
    @SerializedName("valor_total_pagar")
    val valorTotalPagar: Double, // Valor mensal + devoluções
    
    @SerializedName("status_pagamento")
    val statusPagamento: String, // "PENDENTE", "PAGO", "ATRASADO"
    
    @SerializedName("data_vencimento")
    val dataVencimento: String?, // Data de vencimento do pagamento
    
    @SerializedName("data_pagamento")
    val dataPagamento: String?, // Data em que foi marcado como pago
    
    @SerializedName("observacoes")
    val observacoes: String?,
    
    @SerializedName("ticket_medio")
    val ticketMedio: Double,
    
    // Lista de contratos detalhados
    @SerializedName("contratos_detalhes")
    val contratosDetalhes: List<ContratoResumo> = emptyList(),
    
    // Lista de devoluções detalhadas
    @SerializedName("devolucoes_detalhes")
    val devolucoesDetalhes: List<DevolucaoResumo> = emptyList()
) {
    
    /**
     * Retorna o valor mensal formatado como moeda
     */
    fun getValorMensalFormatado(): String {
        return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valorMensal)
    }
    
    /**
     * Retorna o valor total a pagar formatado como moeda
     */
    fun getValorTotalPagarFormatado(): String {
        return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valorTotalPagar)
    }
    
    /**
     * Retorna o valor das devoluções formatado como moeda
     */
    fun getValorDevolucoesFormatado(): String {
        return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valorDevolucoes)
    }
    
    /**
     * Retorna o mês de referência formatado
     */
    fun getMesReferenciaFormatado(): String {
        return try {
            val formato = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val formatoSaida = SimpleDateFormat("MMMM/yyyy", Locale("pt", "BR"))
            val data = formato.parse(mesReferencia)
            formatoSaida.format(data ?: Date())
        } catch (e: Exception) {
            mesReferencia
        }
    }
    
    /**
     * Verifica se o pagamento está em atraso
     */
    fun isPagamentoAtrasado(): Boolean {
        if (statusPagamento == "PAGO") return false
        
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataVenc = dataVencimento?.let { formato.parse(it) }
            dataVenc?.before(Date()) ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Retorna a cor do status baseado no pagamento
     */
    fun getCorStatus(): String {
        return when {
            statusPagamento == "PAGO" -> "#4CAF50" // Verde
            isPagamentoAtrasado() -> "#F44336" // Vermelho
            else -> "#FF9800" // Laranja
        }
    }
    
    /**
     * Retorna ícone do status
     */
    fun getIconeStatus(): String {
        return when {
            statusPagamento == "PAGO" -> "✅"
            isPagamentoAtrasado() -> "❌"
            else -> "⏰"
        }
    }
}

/**
 * Resumo de contrato para o relatório mensal
 */
data class ContratoResumo(
    @SerializedName("contrato_id")
    val contratoId: Int,
    
    @SerializedName("contrato_num")
    val contratoNum: String,
    
    @SerializedName("valor_mensal")
    val valorMensal: Double,
    
    @SerializedName("periodo")
    val periodo: String,
    
    @SerializedName("data_assinatura")
    val dataAssinatura: String?,
    
    @SerializedName("status")
    val status: String
)

/**
 * Resumo de devolução para o relatório mensal
 */
data class DevolucaoResumo(
    @SerializedName("devolucao_id")
    val devolucaoId: Int,
    
    @SerializedName("numero_devolucao")
    val numeroDevolucao: String,
    
    @SerializedName("valor_multa")
    val valorMulta: Double,
    
    @SerializedName("data_devolucao")
    val dataDevolucao: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("equipamento_nome")
    val equipamentoNome: String?
)

/**
 * Request para confirmar pagamento
 */
data class ConfirmarPagamentoRequest(
    @SerializedName("cliente_id")
    val clienteId: Int,
    
    @SerializedName("mes_referencia")
    val mesReferencia: String,
    
    @SerializedName("valor_pago")
    val valorPago: Double,
    
    @SerializedName("data_pagamento")
    val dataPagamento: String,
    
    @SerializedName("observacoes")
    val observacoes: String?
)

/**
 * Response da confirmação de pagamento
 */
data class ConfirmarPagamentoResponse(
    @SerializedName("sucesso")
    val sucesso: Boolean,
    
    @SerializedName("mensagem")
    val mensagem: String,
    
    @SerializedName("resumo_atualizado")
    val resumoAtualizado: ResumoMensalCliente?
) 