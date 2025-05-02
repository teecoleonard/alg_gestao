package com.example.alg_gestao_02.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entidade Room para armazenar informações de contratos de projetos.
 * Possui índices em projectId e type para otimizar consultas por esses campos.
 */
@Entity(
    tableName = "project_contracts",
    indices = [
        Index("projectId"),
        Index("type")
    ]
)
data class ProjectContractEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val name: String,
    val description: String,
    val value: Double,
    val date: String,
    val status: String,
    val type: String,
    
    // Campos extras para controle de sincronização
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
) 