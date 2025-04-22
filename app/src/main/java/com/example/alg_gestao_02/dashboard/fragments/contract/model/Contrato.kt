package com.example.alg_gestao_02.dashboard.fragments.contract.model

data class Contrato(
    val id: String,
    val contractNumber: String,
    val companyName: String,
    val value: Double,
    val startDate: String,
    val endDate: String,
    val status: String
) 