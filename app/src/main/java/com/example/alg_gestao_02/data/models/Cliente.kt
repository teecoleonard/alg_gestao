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
    val endereco: String,
    
    @SerializedName("bairro")
    val bairro: String,
    
    @SerializedName("cep")
    val cep: String? = null,
    
    @SerializedName("cidade")
    val cidade: String,
    
    @SerializedName("estado")
    val estado: String,
    
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
        val enderecoCompleto = StringBuilder(endereco)
        if (bairro.isNotBlank()) {
            enderecoCompleto.append(", $bairro")
        }
        if (!cep.isNullOrBlank()) {
            enderecoCompleto.append(", CEP: $cep")
        }
        enderecoCompleto.append(", $cidade/$estado")
        return enderecoCompleto.toString()
    }
}