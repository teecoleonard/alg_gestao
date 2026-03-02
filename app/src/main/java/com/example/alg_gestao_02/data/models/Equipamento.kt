package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Modelo de dados para Equipamento
 */
@Parcelize
data class Equipamento(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("nomeEquip")
    val nomeEquip: String,
    
    @SerializedName("precoDiaria")
    val precoDiaria: Double,
    
    @SerializedName("precoSemanal")
    val precoSemanal: Double,
    
    @SerializedName("precoQuinzenal")
    val precoQuinzenal: Double,
    
    @SerializedName("precoMensal")
    val precoMensal: Double,
    
    @SerializedName("codigoEquip")
    val codigoEquip: String,
    
    @SerializedName("quantidadeDisp")
    val quantidadeDisp: Int,
    
    @SerializedName("valorPatrimonio")
    val valorPatrimonio: Double? = null,
    
    // Campos de disponibilidade em tempo real
    @SerializedName("quantidade_total")
    val quantidadeTotal: Int? = null,
    
    @SerializedName("quantidade_em_uso")
    val quantidadeEmUso: Int? = null,
    
    @SerializedName("quantidade_disponivel")
    val quantidadeDisponivel: Int? = null,
    
    @SerializedName("percentual_disponivel")
    val percentualDisponivel: Int? = null
) : Parcelable {
    
    /**
     * Verifica se o equipamento está disponível (pelo menos 1 unidade)
     */
    fun isDisponivel(): Boolean {
        return (quantidadeDisponivel ?: quantidadeDisp) > 0
    }
    
    /**
     * Verifica se há quantidade suficiente disponível
     */
    fun temQuantidadeDisponivel(quantidade: Int): Boolean {
        val disponivelAtual = quantidadeDisponivel ?: quantidadeDisp
        return disponivelAtual >= quantidade
    }
    
    /**
     * Retorna a quantidade disponível atual (usa o valor em tempo real se disponível)
     */
    fun getQuantidadeDisponivelAtual(): Int {
        return quantidadeDisponivel ?: quantidadeDisp
    }
    
    /**
     * Retorna o status de disponibilidade em texto
     */
    fun getStatusDisponibilidade(): String {
        val disponivel = quantidadeDisponivel ?: quantidadeDisp
        val emUso = quantidadeEmUso ?: 0
        
        return when {
            disponivel == 0 -> "Indisponível"
            disponivel == quantidadeDisp -> "Disponível"
            else -> "$emUso em uso"
        }
    }
}