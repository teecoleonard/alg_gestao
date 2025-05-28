package com.example.alg_gestao_02.data.dto

data class AssinaturaRequestDTO(
    val base64Data: String,
    val contratoId: Int,
    val clienteId: Int
) 