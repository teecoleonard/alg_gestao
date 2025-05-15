package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Modelo de dados para Equipamento
 */
@Parcelize
data class Equipamento(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("nomeEquip")
    val nomeEquip: String,
    
    @SerializedName("precoDiaria")
    val precoDiaria: Double,
    
    @SerializedName("precoSemanal")
    val precoSemanal: Double,
    
    @SerializedName("precoQuinzenal")
    val precoQuinzenal: Double,
    
    @SerializedName("precoMensal")
    val precoMensal: Double,
    
    @SerializedName("codigoEquip")
    val codigoEquip: String,
    
    @SerializedName("quantidadeDisp")
    val quantidadeDisp: Int,
    
    @SerializedName("valorPatrimonio")
    val valorPatrimonio: Double? = null
) : Parcelable