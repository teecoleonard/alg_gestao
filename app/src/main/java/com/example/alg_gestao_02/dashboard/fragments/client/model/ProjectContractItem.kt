package com.example.alg_gestao_02.dashboard.fragments.client.model

import com.google.gson.annotations.SerializedName

/**
 * Representa um item de contrato dentro de um projeto
 */
data class ProjectContractItem(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("value")
    val value: Double,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("status")
    val status: String, // "active", "pending", "inactive"
    
    @SerializedName("type")
    val type: String // "payment" ou "debt"
) 