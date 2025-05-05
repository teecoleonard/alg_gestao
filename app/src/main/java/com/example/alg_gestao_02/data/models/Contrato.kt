package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Representa um contrato no sistema
 */
@Parcelize
data class Contrato(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("cliente_id")
    val clienteId: Int,
    
    @SerializedName("contratoNum")
    val contratoNum: String,
    
    @SerializedName("dataHoraEmissao")
    val dataHoraEmissao: String,
    
    @SerializedName("dataVenc")
    val dataVenc: String,
    
    @SerializedName("contratoValor")
    val contratoValor: Double,
    
    @SerializedName("obraLocal")
    val obraLocal: String,
    
    @SerializedName("contratoPeriodo")
    val contratoPeriodo: String,
    
    @SerializedName("entregaLocal")
    val entregaLocal: String,
    
    @SerializedName("respPedido")
    val respPedido: String? = null,
    
    @SerializedName("contratoAss")
    val contratoAss: String? = null,
    
    // Campo adicional para nome do cliente
    @SerializedName("cliente_nome")
    val clienteNome: String? = null,
    
    // Objeto cliente completo que vem aninhado na resposta da API
    @SerializedName("cliente")
    val cliente: Cliente? = null
) : Parcelable {
    
    /**
     * Retorna o valor do contrato formatado como moeda
     */
    fun getValorFormatado(): String {
        val formato = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formato.format(contratoValor)
    }
    
    /**
     * Retorna a data de emissão formatada
     */
    fun getDataEmissaoFormatada(): String {
        try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatoSaida = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val data = formatoEntrada.parse(dataHoraEmissao) ?: Date()
            return formatoSaida.format(data)
        } catch (e: Exception) {
            // Em caso de erro, retorna a string original
            return dataHoraEmissao
        }
    }
    
    /**
     * Retorna a data de vencimento formatada
     */
    fun getDataVencimentoFormatada(): String {
        try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSaida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val data = formatoEntrada.parse(dataVenc) ?: Date()
            return formatoSaida.format(data)
        } catch (e: Exception) {
            // Em caso de erro, retorna a string original
            return dataVenc
        }
    }
    
    /**
     * Verifica se o contrato já está assinado
     */
    fun isAssinado(): Boolean {
        return !contratoAss.isNullOrBlank()
    }
    
    /**
     * Retorna o nome do cliente do contrato
     */
    fun resolverNomeCliente(): String {
        // Preferência pelo cliente aninhado se disponível
        return when {
            cliente?.contratante != null -> cliente.contratante
            clienteNome != null -> clienteNome
            else -> "Cliente #$clienteId"
        }
    }
} 