package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class EquipamentoContrato(
    @SerializedName("id")
    val id: Int = 0, // ID=0 indica um novo equipamento que ainda não tem ID temporário
    
    @SerializedName("contrato_id")
    val contratoId: Int,
    
    @SerializedName("equipamento_id")
    val equipamentoId: Int,
    
    @SerializedName("quantidadeEquip")
    val quantidadeEquip: Int,
    
    @SerializedName("valorUnitario")
    val valorUnitario: Double,
    
    @SerializedName("valorTotal")
    val valorTotal: Double,
    
    @SerializedName("valorFrete")
    val valorFrete: Double,
    
    // Campo para o nome do equipamento vindo da API
    @SerializedName("nomeEquip")
    val equipamentoNome: String? = null,
    
    // Campo para referência ao equipamento completo (não armazenado no banco)
    @SerializedName("equipamento")
    val equipamento: Equipamento? = null,
    
    // Campo para armazenar o período selecionado (apenas no front-end, não vai para o banco)
    @Transient
    val periodoSelecionado: String? = null
) : Parcelable {
    // Uma propriedade que permite obter o ID do contrato
    // para comparação com valores específicos
    val contratoIdEfetivo: Int
        get() = if (contratoId < 0) 0 else contratoId
        
    // Propriedade computada para garantir que temos um nome de equipamento
    val nomeEquipamentoExibicao: String
        get() = equipamentoNome ?: equipamento?.nomeEquip ?: "Equipamento $equipamentoId"

    companion object {
        /**
         * Verifica se um ID é temporário (gerado localmente)
         * IDs temporários são positivos e maiores que 1.000.000
         * para evitar conflitos com IDs reais do servidor
         */
        fun isTempId(id: Int): Boolean {
            return id > 1_000_000
        }
    }
}

// Função de extensão para verificar se o contratoId é válido
fun EquipamentoContrato.temContratoIdValido(): Boolean {
    return contratoId > 0
}

// Função de extensão para verificar se o contratoId é temporário
fun EquipamentoContrato.temContratoIdTemporario(): Boolean {
    return contratoId < 0
}

// Classe auxiliar para deserialização do JSON
@Parcelize
data class EquipamentoJson(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("nomeEquip")
    val nomeEquip: String?,
    
    @SerializedName("precoDiaria")
    val precoDiaria: String?,
    
    @SerializedName("precoSemanal")
    val precoSemanal: String?,
    
    @SerializedName("precoQuinzenal")
    val precoQuinzenal: String?,
    
    @SerializedName("precoMensal")
    val precoMensal: String?,
    
    @SerializedName("codigoEquip")
    val codigoEquip: String?,
    
    @SerializedName("quantidadeDisp")
    val quantidadeDisp: Int?,
    
    @SerializedName("valorPatrimonio")
    val valorPatrimonio: Double?,
    
    @SerializedName("EquipamentoContrato")
    val equipamentoContrato: EquipamentoContratoData?
) : Parcelable

@Parcelize
data class EquipamentoContratoData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("contrato_id")
    val contratoId: Int,
    
    @SerializedName("equipamento_id")
    val equipamentoId: Int,
    
    @SerializedName("quantidadeEquip")
    val quantidadeEquip: Int,
    
    @SerializedName("valorUnitario")
    val valorUnitario: String,
    
    @SerializedName("valorTotal")
    val valorTotal: String,
    
    @SerializedName("valorFrete")
    val valorFrete: String
) : Parcelable
