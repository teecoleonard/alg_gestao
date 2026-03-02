package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Representa um cliente no sistema
 */
@Parcelize
data class Cliente(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("contratante")
    val contratante: String,
    
    @SerializedName("cpf_cnpj")
    val cpfCnpj: String,
    
    @SerializedName("rg_ie")
    val rgIe: String? = null,
    
    @SerializedName("endereco")
    val endereco: String? = null,
    
    @SerializedName("bairro")
    val bairro: String? = null,
    
    @SerializedName("cep")
    val cep: String? = null,
    
    @SerializedName("cidade")
    val cidade: String? = null,
    
    @SerializedName("estado")
    val estado: String? = null,
    
    @SerializedName("telefone")
    val telefone: String? = null
) : Parcelable {
    /**
     * Retorna se o cliente é pessoa física ou jurídica com base no CPF/CNPJ
     */
    fun isPessoaFisica(): Boolean {
        return cpfCnpj.replace("[^0-9]".toRegex(), "").length <= 11
    }
    
    /**
     * Retorna a descrição formatada do documento
     */
    fun getDocumentoFormatado(): String {
        return if (isPessoaFisica()) {
            "CPF: $cpfCnpj"
        } else {
            "CNPJ: $cpfCnpj"
        }
    }
    
    /**
     * Retorna o tipo de documento secundário formatado
     */
    fun getDocumentoSecundarioFormatado(): String {
        return if (isPessoaFisica()) {
            if (rgIe.isNullOrBlank()) "RG: Não informado" else "RG: $rgIe"
        } else {
            if (rgIe.isNullOrBlank()) "IE: Não informado" else "IE: $rgIe"
        }
    }
    
    /**
     * Retorna o endereço completo formatado
     */
    fun getEnderecoCompleto(): String {
        val enderecoCompleto = StringBuilder()
        
        // Adiciona o endereço se não for nulo ou vazio
        if (!endereco.isNullOrBlank()) {
            enderecoCompleto.append(endereco)
        }
        
        // Adiciona o bairro se não for nulo ou vazio
        if (!bairro.isNullOrBlank()) {
            if (enderecoCompleto.isNotEmpty()) {
                enderecoCompleto.append(", ")
            }
            enderecoCompleto.append(bairro)
        }
        
        // Adiciona o CEP se não for nulo ou vazio
        if (!cep.isNullOrBlank()) {
            if (enderecoCompleto.isNotEmpty()) {
                enderecoCompleto.append(", ")
            }
            enderecoCompleto.append("CEP: $cep")
        }
        
        // Adiciona cidade e estado se não forem nulos ou vazios
        val cidadeEstado = mutableListOf<String>()
        if (!cidade.isNullOrBlank()) {
            cidadeEstado.add(cidade)
        }
        if (!estado.isNullOrBlank()) {
            cidadeEstado.add(estado)
        }
        
        if (cidadeEstado.isNotEmpty()) {
            if (enderecoCompleto.isNotEmpty()) {
                enderecoCompleto.append(", ")
            }
            enderecoCompleto.append(cidadeEstado.joinToString("/"))
        }
        
        // Se não há nenhum endereço, retorna uma mensagem padrão
        return if (enderecoCompleto.isEmpty()) {
            "Endereço não informado"
        } else {
            enderecoCompleto.toString()
        }
    }
}