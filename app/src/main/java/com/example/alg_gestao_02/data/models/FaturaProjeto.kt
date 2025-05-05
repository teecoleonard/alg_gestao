package com.example.alg_gestao_02.data.models

import java.util.Date

/**
 * Modelo que representa uma fatura associada a um projeto.
 * Este modelo substitui o antigo ProjectInvoiceItem.
 */
data class FaturaProjeto(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String,
    val value: String,
    val date: String,
    val dueDate: String,
    val status: String,
    val month: Int,
    val year: Int
) 