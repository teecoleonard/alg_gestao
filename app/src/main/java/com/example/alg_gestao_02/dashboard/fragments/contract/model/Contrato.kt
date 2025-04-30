package com.example.alg_gestao_02.dashboard.fragments.contract.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de dados para Contrato
 */
data class Contrato(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("projectId")
    val projectId: String,
    
    @SerializedName("contractNumber")
    val contractNumber: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("client")
    val client: String,
    
    @SerializedName("startDate")
    val startDate: String,
    
    @SerializedName("endDate")
    val endDate: String,
    
    @SerializedName("value")
    val value: Double,
    
    @SerializedName("status")
    val status: String, // "active", "pending", "completed", "cancelled"
    
    @SerializedName("description")
    val description: String? = null
) 