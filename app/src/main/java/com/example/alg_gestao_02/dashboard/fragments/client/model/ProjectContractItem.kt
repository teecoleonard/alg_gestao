package com.example.alg_gestao_02.dashboard.fragments.client.model

/**
 * Modelo de dados para um item de contrato de projeto
 */
data class ProjectContractItem(
    val id: String,
    val projectId: String,
    val name: String,
    val date: String,
    val type: String,
    val status: String,
    val value: Double,
    val description: String = ""
) 