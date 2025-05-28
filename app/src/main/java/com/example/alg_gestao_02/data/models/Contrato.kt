package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.alg_gestao_02.utils.LogUtils

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

    @SerializedName("status_assinatura")
    val status_assinatura: String? = null,

    @SerializedName("data_assinatura")
    val data_assinatura: String? = null,

    // Campo adicional para nome do cliente
    @SerializedName("cliente_nome")
    val clienteNome: String? = null,
    
    // Objeto cliente completo que vem aninhado na resposta da API
    @SerializedName("cliente")
    val cliente: Cliente? = null,
    
    // Lista de equipamentos associados ao contrato
    @SerializedName("equipamentos")
    val equipamentos: List<EquipamentoContrato> = emptyList(),

    // Suporte para resposta da API que retorna como "equipamentoContratos"
    @SerializedName("equipamentoContratos")
    val equipamentoContratos: List<EquipamentoContrato>? = null,

    // Associação com a entidade Assinatura (agora é um objeto aninhado)
    @SerializedName("assinatura")
    val assinatura: Assinatura? = null
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
        return if (jsonEquipamentos.isNullOrEmpty()) {
            LogUtils.debug("Contrato", "Lista de equipamentos JSON nula ou vazia")
            emptyList()
        } else {
            jsonEquipamentos.mapNotNull { json ->
                try {
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
                } catch (e: Exception) {
                    LogUtils.error("Contrato", "Erro ao processar equipamento JSON: ${e.message}")
                    null // Ignora este equipamento se houver erro
                }
            }
        }
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
            return dataVenc ?: "" // dataVenc já é String?
        }
    }
    
    /**
     * Verifica se o contrato já está assinado
     */
    fun isAssinado(): Boolean {
        return status_assinatura == "ASSINADO"
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
    
    /**
     * Retorna o valor efetivo do contrato para exibição.
     * Calculado exclusivamente a partir dos equipamentos quando disponíveis.
     * Se não houver equipamentos, usa o contratoValor fornecido pela API.
     */
    fun getValorEfetivo(): Double {
        // Se houver equipamentos, soma seus valores totais
        return if (!equipamentos.isNullOrEmpty()) {
            LogUtils.debug("Contrato", "Calculando valor efetivo a partir de ${equipamentos.size} equipamentos")
            equipamentos.sumOf { it.valorTotal }
        } else {
            // Usar o contratoValor fornecido pela API (que já está calculado no servidor)
            LogUtils.debug("Contrato", "Usando contratoValor da API: $contratoValor")
            contratoValor
        }
    }
    
    /**
     * Sempre retorna a lista de equipamentos mais completa para exibição e lógica.
     * Prioriza a lista 'equipamentos', se não estiver vazia, senão usa 'equipamentoContratos'.
     */
    val equipamentosParaExibicao: List<EquipamentoContrato>
        get() = when {
            !equipamentos.isNullOrEmpty() -> equipamentos
            !equipamentoContratos.isNullOrEmpty() -> equipamentoContratos!!
            else -> emptyList()
        }

    /**
     * Verifica se o contrato pode ser excluído com base no status e papel do usuário
     * @param isAdmin Indica se o usuário tem papel de administrador
     * @param forcar Indica se a exclusão deve ser forçada (apenas para admin)
     * @return Pair<Boolean, String> onde o primeiro valor indica se pode excluir e o segundo a mensagem explicativa
     */
    fun podeExcluir(isAdmin: Boolean, forcar: Boolean = false): Pair<Boolean, String> {
        return when {
            // Se for admin e forçar, permite exclusão independente do status
            isAdmin && forcar -> Pair(true, "Exclusão forçada permitida para administrador")
            
            // Se for admin mas não forçar, verifica regras normais
            isAdmin -> when (status_assinatura) {
                "ASSINADO" -> Pair(false, "Contrato assinado não pode ser excluído")
                "PENDENTE" -> Pair(true, "Contrato pendente pode ser excluído")
                else -> Pair(true, "Contrato não assinado pode ser excluído")
            }
            
            // Se não for admin, só permite excluir contratos não assinados
            else -> when (status_assinatura) {
                "ASSINADO" -> Pair(false, "Apenas administradores podem excluir contratos assinados")
                "PENDENTE" -> Pair(false, "Apenas administradores podem excluir contratos pendentes")
                else -> Pair(true, "Contrato não assinado pode ser excluído")
            }
        }
    }

    /**
     * Retorna uma mensagem explicativa sobre a possibilidade de exclusão
     */
    fun getMensagemExclusao(isAdmin: Boolean): String {
        return when (status_assinatura) {
            "ASSINADO" -> if (isAdmin) {
                "Contrato assinado. Apenas administradores podem forçar a exclusão."
            } else {
                "Contrato assinado não pode ser excluído."
            }
            "PENDENTE" -> if (isAdmin) {
                "Contrato pendente. Apenas administradores podem excluir."
            } else {
                "Contrato pendente não pode ser excluído."
            }
            else -> "Contrato não assinado pode ser excluído."
        }
    }
}

// Trigger recompile for Parcelize
