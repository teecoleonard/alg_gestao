package com.example.alg_gestao_02.data.db.mapper

import com.example.alg_gestao_02.data.models.ContratoProjeto
import com.example.alg_gestao_02.data.db.entity.ProjectContractEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Classe responsável por mapear entre entidades de banco de dados e modelos de domínio
 * para contratos de projetos.
 */
class ProjectContractMapper {
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    /**
     * Converte uma entidade de contrato para um objeto de domínio
     */
    fun fromEntity(entity: ProjectContractEntity): ContratoProjeto {
        return ContratoProjeto(
            id = entity.id,
            projectId = entity.projectId,
            name = entity.name,
            clientName = entity.description,
            value = formatCurrency(entity.value),
            date = entity.date,
            status = entity.status,
            type = entity.type
        )
    }
    
    /**
     * Converte um objeto de domínio para uma entidade de contrato
     */
    fun toEntity(domain: ContratoProjeto, isSynced: Boolean = true): ProjectContractEntity {
        return ProjectContractEntity(
            id = domain.id,
            projectId = domain.projectId,
            name = domain.name,
            description = domain.clientName,
            value = parseCurrency(domain.value),
            date = domain.date,
            status = domain.status,
            type = domain.type,
            lastUpdated = System.currentTimeMillis(),
            isSynced = isSynced
        )
    }
    
    /**
     * Converte uma lista de entidades para uma lista de objetos de domínio
     */
    fun fromEntityList(entities: List<ProjectContractEntity>): List<ContratoProjeto> {
        return entities.map { fromEntity(it) }
    }
    
    /**
     * Converte uma lista de objetos de domínio para uma lista de entidades
     */
    fun toEntityList(domains: List<ContratoProjeto>, isSynced: Boolean = true): List<ProjectContractEntity> {
        return domains.map { toEntity(it, isSynced) }
    }
    
    /**
     * Formata um número para moeda (R$)
     */
    private fun formatCurrency(value: Double): String {
        return "R$ ${String.format("%.2f", value)}"
    }
    
    /**
     * Converte string de moeda para Double
     */
    private fun parseCurrency(value: String): Double {
        return try {
            value.replace("R$", "")
                .replace(".", "")
                .replace(",", ".")
                .trim()
                .toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
    
    /**
     * Formata uma data para o formato dd/MM/yyyy
     */
    private fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
} 