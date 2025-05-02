package com.example.alg_gestao_02.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.alg_gestao_02.data.db.entity.ProjectContractEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operações com a entidade ProjectContractEntity.
 */
@Dao
interface ProjectContractDao {
    
    /**
     * Insere um contrato no banco de dados.
     * Em caso de conflito (mesmo ID), substitui o registro existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contract: ProjectContractEntity)
    
    /**
     * Insere vários contratos no banco de dados.
     * Em caso de conflito (mesmo ID), substitui os registros existentes.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contracts: List<ProjectContractEntity>)
    
    /**
     * Atualiza um contrato existente no banco de dados.
     */
    @Update
    suspend fun update(contract: ProjectContractEntity)
    
    /**
     * Remove um contrato do banco de dados.
     */
    @Delete
    suspend fun delete(contract: ProjectContractEntity)
    
    /**
     * Remove um contrato pelo ID.
     */
    @Query("DELETE FROM project_contracts WHERE id = :contractId")
    suspend fun deleteById(contractId: String)
    
    /**
     * Obtém todos os contratos de um projeto específico.
     * Retorna um Flow para receber atualizações em tempo real.
     */
    @Query("SELECT * FROM project_contracts WHERE projectId = :projectId ORDER BY date DESC")
    fun getContractsByProjectId(projectId: String): Flow<List<ProjectContractEntity>>
    
    /**
     * Obtém todos os contratos de um projeto por tipo (payment, debt).
     */
    @Query("SELECT * FROM project_contracts WHERE projectId = :projectId AND type = :type ORDER BY date DESC")
    fun getContractsByProjectIdAndType(projectId: String, type: String): Flow<List<ProjectContractEntity>>
    
    /**
     * Busca contratos por texto em diferentes campos.
     */
    @Query("SELECT * FROM project_contracts WHERE projectId = :projectId AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')")
    fun searchContracts(projectId: String, query: String): Flow<List<ProjectContractEntity>>
    
    /**
     * Obtém contratos não sincronizados com o servidor.
     */
    @Query("SELECT * FROM project_contracts WHERE isSynced = 0")
    suspend fun getUnsyncedContracts(): List<ProjectContractEntity>
    
    /**
     * Obtém um contrato específico pelo ID.
     */
    @Query("SELECT * FROM project_contracts WHERE id = :contractId")
    suspend fun getContractById(contractId: String): ProjectContractEntity?
    
    /**
     * Marca um contrato como sincronizado.
     */
    @Query("UPDATE project_contracts SET isSynced = 1 WHERE id = :contractId")
    suspend fun markAsSynced(contractId: String)
} 