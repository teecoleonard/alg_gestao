package com.example.alg_gestao_02.dashboard.fragments.client.model

import com.google.gson.annotations.SerializedName

/**
 * Representa um item de fatura dentro de um projeto
 */
data class ProjectInvoiceItem(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("numero")
    val numero: String,
    
    @SerializedName("projectId")
    val projectId: String,
    
    @SerializedName("valor")
    val valor: Double,
    
    @SerializedName("dataEmissao")
    val dataEmissao: String,
    
    @SerializedName("dataVencimento")
    val dataVencimento: String,
    
    @SerializedName("status")
    val status: String // "Pendente", "Pago", "Atrasado", "Cancelado"
) 