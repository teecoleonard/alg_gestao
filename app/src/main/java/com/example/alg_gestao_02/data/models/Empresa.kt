package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Representa uma empresa no sistema
 */
data class Empresa(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("documento")
    val cnpj: String,
    
    @SerializedName("telefone")
    val telefone: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("endereco")
    val endereco: String,
    
    @SerializedName("nome_fantasia")
    val nomeFantasia: String,
    
    @SerializedName("razao_social")
    val razaoSocial: String,
    
    @SerializedName("inscricao_estadual")
    val inscricaoEstadual: String? = null,
    
    @SerializedName("ramo_atividade")
    val ramoAtividade: String? = null,
    
    @SerializedName("observacoes")
    val observacoes: String? = null,
    
    @SerializedName("status")
    val status: String = "ativo" // "ativo" ou "inativo"
) {
    /**
     * Retorna o nome de exibição da empresa.
     * Retorna nome fantasia se disponível, senão razão social ou nome
     */
    fun getNomeExibicao(): String {
        return nomeFantasia.ifEmpty { razaoSocial.ifEmpty { nome } }
    }
    
    /**
     * Retorna o CNPJ formatado
     */
    fun getCnpjFormatado(): String {
        return "CNPJ: $cnpj"
    }
} 