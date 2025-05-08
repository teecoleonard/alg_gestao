package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Response wrapper for contratos API calls
 */
data class ContratoResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("contrato")
    val contrato: Contrato
)
