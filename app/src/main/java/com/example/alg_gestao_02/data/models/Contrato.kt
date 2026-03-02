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

    @SerializedName("status_contrato")
    val statusContrato: String? = "PENDENTE",

    // Campo para arquivamento de contratos
    @SerializedName("arquivado")
    val arquivado: Boolean = false,

    // Data de arquivamento
    @SerializedName("data_arquivamento")
    val dataArquivamento: String? = null,

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
     * Retorna o status do contrato como enum
     */
    fun getStatusContratoEnum(): StatusContrato {
        return StatusContrato.fromString(statusContrato)
    }
    
    /**
     * Verifica se o contrato está finalizado (todos equipamentos devolvidos)
     */
    fun isFinalizado(): Boolean {
        return statusContrato == "FINALIZADO"
    }
    
    /**
     * Verifica se o contrato está em andamento
     */
    fun isEmAndamento(): Boolean {
        return statusContrato == "EM_ANDAMENTO"
    }
    
    /**
     * Verifica se o contrato está arquivado
     */
    fun isArquivado(): Boolean {
        return arquivado
    }
    
    /**
     * Verifica se o contrato deve ser arquivado automaticamente
     * (Finalizado há mais de 6 meses)
     */
    fun deveSerArquivado(): Boolean {
        if (!isFinalizado()) return false
        if (arquivado) return false
        
        try {
            val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataFinalizacao = dataVenc?.let { formatoData.parse(it) } ?: return false
            
            val calendar = java.util.Calendar.getInstance()
            calendar.time = dataFinalizacao
            calendar.add(java.util.Calendar.MONTH, 6)
            
            return Date().after(calendar.time)
        } catch (e: Exception) {
            LogUtils.error("Contrato", "Erro ao verificar arquivamento automático", e)
            return false
        }
    }
    
    /**
     * Verifica se o contrato está vencido
     */
    fun isVencido(): Boolean {
        if (dataVenc.isNullOrEmpty()) return false
        
        try {
            val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataVencimento = formatoData.parse(dataVenc) ?: return false
            val hoje = Date()
            
            return hoje.after(dataVencimento) && !isFinalizado()
        } catch (e: Exception) {
            LogUtils.error("Contrato", "Erro ao verificar vencimento", e)
            return false
        }
    }
    
    /**
     * Verifica se o contrato está próximo do vencimento (7 dias ou menos)
     */
    fun isProximoVencimento(): Boolean {
        if (dataVenc.isNullOrEmpty()) return false
        if (isFinalizado()) return false
        
        try {
            val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataVencimento = formatoData.parse(dataVenc) ?: return false
            val hoje = Date()
            
            if (hoje.after(dataVencimento)) return false
            
            val diferencaMilissegundos = dataVencimento.time - hoje.time
            val diferencaDias = diferencaMilissegundos / (1000 * 60 * 60 * 24)
            
            return diferencaDias <= 7
        } catch (e: Exception) {
            LogUtils.error("Contrato", "Erro ao verificar proximidade do vencimento", e)
            return false
        }
    }
    
    /**
     * Retorna os dias até o vencimento
     */
    fun getDiasAteVencimento(): Long {
        if (dataVenc.isNullOrEmpty()) return 0
        
        try {
            val formatoData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataVencimento = formatoData.parse(dataVenc) ?: return 0
            val hoje = Date()
            
            val diferencaMilissegundos = dataVencimento.time - hoje.time
            return diferencaMilissegundos / (1000 * 60 * 60 * 24)
        } catch (e: Exception) {
            LogUtils.error("Contrato", "Erro ao calcular dias até vencimento", e)
            return 0
        }
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
