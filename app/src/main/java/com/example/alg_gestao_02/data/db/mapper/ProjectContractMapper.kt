package com.example.alg_gestao_02.data.db.mapper

import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import com.example.alg_gestao_02.data.db.entity.ProjectContractEntity

/**
 * Classe utilitária para mapear conversões entre entidades do Room e modelos de domínio.
 */
object ProjectContractMapper {
    
    /**
     * Converte uma entidade do Room para um modelo de domínio.
     *
     * @param entity A entidade Room de contrato de projeto.
     * @return Um modelo de domínio de contrato de projeto.
     */
    fun fromEntity(entity: ProjectContractEntity): ProjectContractItem {
        return ProjectContractItem(
            id = entity.id,
            projectId = entity.projectId,
            name = entity.name,
            description = entity.description,
            value = entity.value,
            date = entity.date,
            status = entity.status,
            type = entity.type
        )
    }
    
    /**
     * Converte um modelo de domínio para uma entidade do Room.
     *
     * @param domain O modelo de domínio de contrato de projeto.
     * @param isSynced Flag indicando se os dados estão sincronizados com o servidor.
     * @return Uma entidade Room de contrato de projeto.
     */
    fun toEntity(domain: ProjectContractItem, isSynced: Boolean = true): ProjectContractEntity {
        return ProjectContractEntity(
            id = domain.id,
            projectId = domain.projectId,
            name = domain.name,
            description = domain.description,
            value = domain.value,
            date = domain.date,
            status = domain.status,
            type = domain.type,
            lastUpdated = System.currentTimeMillis(),
            isSynced = isSynced
        )
    }
    
    /**
     * Converte uma lista de entidades do Room para uma lista de modelos de domínio.
     *
     * @param entities Lista de entidades Room de contratos de projeto.
     * @return Lista de modelos de domínio de contratos de projeto.
     */
    fun fromEntityList(entities: List<ProjectContractEntity>): List<ProjectContractItem> {
        return entities.map { fromEntity(it) }
    }
    
    /**
     * Converte uma lista de modelos de domínio para uma lista de entidades do Room.
     *
     * @param domains Lista de modelos de domínio de contratos de projeto.
     * @param isSynced Flag indicando se os dados estão sincronizados com o servidor.
     * @return Lista de entidades Room de contratos de projeto.
     */
    fun toEntityList(domains: List<ProjectContractItem>, isSynced: Boolean = true): List<ProjectContractEntity> {
        return domains.map { toEntity(it, isSynced) }
    }
} 