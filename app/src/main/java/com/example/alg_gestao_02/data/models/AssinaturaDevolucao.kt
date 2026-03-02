package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Representa uma assinatura de devolução no sistema
 */
@Parcelize
data class AssinaturaDevolucao(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("devolucao_id")
    val devolucaoId: Int,
    
    @SerializedName("nome_arquivo")
    val nomeArquivo: String,
    
    @SerializedName("data_criacao")
    val dataCriacao: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
) : Parcelable
