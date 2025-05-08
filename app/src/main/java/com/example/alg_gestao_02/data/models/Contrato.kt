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
    val contratoNum: String? = "",
    
    @SerializedName("dataHoraEmissao")
    val dataHoraEmissao: String? = "",
    
    @SerializedName("dataVenc")
    val dataVenc: String? = "",
    
    @SerializedName("contratoValor")
    val contratoValor: Double,
    
    @SerializedName("obraLocal")
    val obraLocal: String? = "",
    
    @SerializedName("contratoPeriodo")
    val contratoPeriodo: String? = "",
    
    @SerializedName("entregaLocal")
    val entregaLocal: String? = "",
    
    @SerializedName("respPedido")
    val respPedido: String? = null,
    
    @SerializedName("contratoAss")
    val contratoAss: String? = null,
    
    // Campo adicional para nome do cliente
    @SerializedName("cliente_nome")
    val clienteNome: String? = null,
    
    // Objeto cliente completo que vem aninhado na resposta da API
    @SerializedName("cliente")
    val cliente: Cliente? = null,
    
    // Lista de equipamentos associados ao contrato
    @SerializedName("equipamentos")
    val equipamentos: List<EquipamentoContrato> = emptyList()
) : Parcelable {
    
    companion object {
        /**
         * Gera um ID temporário único para contratos não salvos no servidor
         * IDs temporários são negativos para não conflitar com IDs reais (positivos)
         */
        fun generateTempId(): Int {
            val timestamp = System.currentTimeMillis()
            return -((timestamp % Int.MAX_VALUE).toInt() + 1) // Garante <0 e único
        }
        
        /**
         * Verifica se um ID de contrato é temporário
         */
        fun isTempId(id: Int): Boolean {
            return id < 0
        }
    }
    
    /**
     * Converte os dados do JSON para o modelo EquipamentoContrato
     */
    fun processarEquipamentosJson(jsonEquipamentos: List<EquipamentoJson>?): List<EquipamentoContrato> {
        return jsonEquipamentos?.map { json ->
            EquipamentoContrato(
                id = json.equipamentoContrato?.id ?: -1,
                contratoId = json.equipamentoContrato?.contratoId ?: 0,
                equipamentoId = json.id,
                quantidadeEquip = json.equipamentoContrato?.quantidadeEquip ?: 0,
                valorUnitario = json.equipamentoContrato?.valorUnitario?.toDoubleOrNull() ?: 0.0,
                valorTotal = json.equipamentoContrato?.valorTotal?.toDoubleOrNull() ?: 0.0,
                valorFrete = json.equipamentoContrato?.valorFrete?.toDoubleOrNull() ?: 0.0,
                equipamentoNome = json.nomeEquip
            )
        } ?: emptyList()
    }
    
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
        if (dataHoraEmissao.isNullOrEmpty()) return ""
        
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
        if (dataVenc.isNullOrEmpty()) return ""
        
        try {
            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoSaida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val data = formatoEntrada.parse(dataVenc) ?: Date()
            return formatoSaida.format(data)
        } catch (e: Exception) {
            // Em caso de erro, retorna a string original
            return dataVenc ?: ""
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
        return clienteNome ?: cliente?.contratante ?: "Cliente não encontrado"
    }
    
    /**
     * Retorna o número do contrato ou string vazia se nulo
     */
    fun getContratoNumOuVazio(): String {
        return contratoNum ?: ""
    }
}
