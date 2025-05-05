package com.example.alg_gestao_02.data.models

/**
 * Modelo que representa um contrato associado a um projeto.
 * Este modelo substitui o antigo ProjectContractItem.
 */
data class ContratoProjeto(
    val id: String,
    val projectId: String,
    val name: String,
    val clientName: String,
    val value: String,
    val date: String,
    val status: String,
    val type: String
) 