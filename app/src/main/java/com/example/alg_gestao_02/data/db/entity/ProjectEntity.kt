package com.example.alg_gestao_02.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entidade Room para armazenar informações básicas de projetos.
 */
@Entity(
    tableName = "projects"
)
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val clientId: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val totalPaid: Double = 0.0,
    val totalDue: Double = 0.0,
    
    // Campos extras para controle de sincronização
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
) 