package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de dados para as estatísticas do dashboard
 */
data class DashboardStats(
    @SerializedName("contratos")
    val contratos: Int,
    
    @SerializedName("clientes")
    val clientes: Int,
    
    @SerializedName("equipamentos")
    val equipamentos: Int,
    
    @SerializedName("devolucoes")
    val devolucoes: Int
) 