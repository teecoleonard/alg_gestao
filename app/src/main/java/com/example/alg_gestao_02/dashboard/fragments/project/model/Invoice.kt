package com.example.alg_gestao_02.dashboard.fragments.project.model

import java.util.Date

data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val projectId: String,
    val value: Double,
    val issueDate: Date,
    val dueDate: Date,
    val status: InvoiceStatus
)

enum class InvoiceStatus {
    PENDING,
    PAID,
    OVERDUE,
    CANCELLED
} 