package com.example.alg_gestao_02.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entidade Room para armazenar informações de faturas de projetos.
 * Possui índices em projectId e status para otimizar consultas por esses campos.
 */
@Entity(
    tableName = "project_invoices",
    indices = [
        Index("projectId"),
        Index("status")
    ]
)
data class ProjectInvoiceEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val numero: String,
    val valor: Double,
    val dataEmissao: String,
    val dataVencimento: String,
    val status: String,
    
    // Campos extras para controle de sincronização
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
) 