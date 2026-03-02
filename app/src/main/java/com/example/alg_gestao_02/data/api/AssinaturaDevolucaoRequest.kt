package com.example.alg_gestao_02.data.api

data class AssinaturaDevolucaoRequest(
    val base64Data: String,
    val devolucaoId: Int
)

data class AssinaturaDevolucaoResponse(
    val success: Boolean,
    val message: String,
    val data: AssinaturaDevolucaoData? = null
)

data class AssinaturaDevolucaoData(
    val assinaturaId: Int,
    val devolucaoId: Int,
    val nomeArquivo: String,
    val statusAssinatura: String,
    val dataAssinatura: String
)
