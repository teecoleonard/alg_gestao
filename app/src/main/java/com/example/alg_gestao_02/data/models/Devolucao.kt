package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.DateTimeUtils
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Representa um item de devolução no sistema
 */
@Parcelize
data class Devolucao(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("contrato_id")
    val contratoId: Int,
    
    @SerializedName("cliente_id")
    val clienteId: Int,
    
    @SerializedName("equipamento_id")
    val equipamentoId: Int,
    
    @SerializedName("dev_num")
    val devNum: String,
    
    @SerializedName("data_devolucao_prevista")
    val dataDevolucaoPrevista: String? = "",
    
    @SerializedName("data_devolucao_efetiva")
    val dataDevolucaoEfetiva: String? = null,
    
    @SerializedName("quantidade_contratada")
    val quantidadeContratada: Int = 0,
    
    @SerializedName("quantidade_devolvida")
    val quantidadeDevolvida: Int = 0,
    
    @SerializedName("status_item_devolucao")
    val statusItemDevolucao: String = "Pendente",
    
    @SerializedName("observacao_item_devolucao")
    val observacaoItemDevolucao: String? = null,
    
    // Objeto contrato completo que vem aninhado na resposta da API
    @SerializedName("contrato")
    val contrato: Contrato? = null,
    
    // Objeto cliente que vem aninhado na resposta da API
    @SerializedName("cliente")
    val cliente: Cliente? = null,
    
    // Objeto equipamento que vem aninhado na resposta da API
    @SerializedName("equipamento")
    val equipamento: Equipamento? = null,
    
    // Objeto assinatura que vem aninhado na resposta da API
    @SerializedName("assinatura")
    val assinatura: AssinaturaDevolucao? = null
) : Parcelable {
    
    /**
     * Retorna o nome do cliente associado à devolução
     */
    fun resolverNomeCliente(): String {
        return cliente?.contratante 
            ?: contrato?.resolverNomeCliente() 
            ?: "Cliente não encontrado"
    }
    
    /**
     * Retorna o nome do equipamento associado à devolução
     */
    fun resolverNomeEquipamento(): String {
        LogUtils.info("Devolucao", "🔍 Resolvendo nome do equipamento - ID: $id")
        LogUtils.info("Devolucao", "📦 Equipamento objeto: $equipamento")
        LogUtils.info("Devolucao", "🏷️ Equipamento nome: ${equipamento?.nomeEquip}")
        
        return equipamento?.nomeEquip ?: "Equipamento não encontrado"
    }
    
    /**
     * Retorna a data prevista de devolução formatada
     */
    fun getDataPrevistaFormatada(): String {
        if (dataDevolucaoPrevista.isNullOrEmpty()) return ""
        return DateTimeUtils.formatarDataISO(dataDevolucaoPrevista, "dd/MM/yyyy")
    }
    
    /**
     * Retorna a data efetiva de devolução formatada, se existir
     */
    fun getDataEfetivaFormatada(): String {
        if (dataDevolucaoEfetiva.isNullOrEmpty()) return "Não devolvido"
        return DateTimeUtils.formatarDataISO(dataDevolucaoEfetiva, "dd/MM/yyyy HH:mm")
    }
    
    /**
     * Retorna o status da devolução formatado
     */
    fun getStatusFormatado(): String {
        return when (statusItemDevolucao) {
            "Pendente" -> "Pendente"
            "Devolvido" -> "Devolvido"
            "Avariado" -> "Avariado"
            "Faltante" -> "Faltante"
            else -> statusItemDevolucao
        }
    }
    
    /**
     * Verifica se a devolução está pendente
     */
    fun isPendente(): Boolean {
        return statusItemDevolucao == "Pendente"
    }
    
    /**
     * Verifica se a devolução já foi processada (devolvido, avariado ou faltante)
     */
    fun isProcessado(): Boolean {
        return !isPendente()
    }
    
    /**
     * Retorna a quantidade pendente de devolução
     */
    fun getQuantidadePendente(): Int {
        return quantidadeContratada - quantidadeDevolvida
    }
    
    /**
     * Verifica se há quantidade pendente para devolução
     */
    fun temQuantidadePendente(): Boolean {
        return getQuantidadePendente() > 0
    }
}
