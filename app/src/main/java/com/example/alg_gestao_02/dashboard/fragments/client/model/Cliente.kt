package com.example.alg_gestao_02.dashboard.fragments.client.model

import com.google.gson.annotations.SerializedName

/**
 * Representa um cliente no sistema
 */
data class Cliente(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("nome")
    val nome: String,
    
    @SerializedName("documento")
    val documento: String,
    
    @SerializedName("telefone")
    val telefone: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("endereco")
    val endereco: String,
    
    @SerializedName("tipo")
    val tipo: String // "PF" ou "PJ"
) 