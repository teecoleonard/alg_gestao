package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Representa um usuário do sistema
 */
data class User(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("cpf")
    val cpf: String,
    
    @SerializedName("nome")
    val name: String,
    
    @SerializedName("role")
    val role: String
) 