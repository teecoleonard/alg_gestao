package com.example.alg_gestao_02.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.alg_gestao_02.data.db.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operações com a entidade ProjectEntity.
 */
@Dao
interface ProjectDao {
    
    /**
     * Insere um projeto no banco de dados.
     * Em caso de conflito (mesmo ID), substitui o registro existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)
    
    /**
     * Insere vários projetos no banco de dados.
     * Em caso de conflito (mesmo ID), substitui os registros existentes.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<ProjectEntity>)
    
    /**
     * Atualiza um projeto existente no banco de dados.
     */
    @Update
    suspend fun update(project: ProjectEntity)
    
    /**
     * Remove um projeto do banco de dados.
     */
    @Delete
    suspend fun delete(project: ProjectEntity)
    
    /**
     * Remove um projeto pelo ID.
     */
    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteById(projectId: String)
    
    /**
     * Obtém todos os projetos.
     * Retorna um Flow para receber atualizações em tempo real.
     */
    @Query("SELECT * FROM projects ORDER BY name ASC")
    fun getAllProjects(): Flow<List<ProjectEntity>>
    
    /**
     * Obtém todos os projetos de um cliente específico.
     */
    @Query("SELECT * FROM projects WHERE clientId = :clientId ORDER BY name ASC")
    fun getProjectsByClientId(clientId: String): Flow<List<ProjectEntity>>
    
    /**
     * Obtém todos os projetos com um status específico.
     */
    @Query("SELECT * FROM projects WHERE status = :status ORDER BY name ASC")
    fun getProjectsByStatus(status: String): Flow<List<ProjectEntity>>
    
    /**
     * Busca projetos por texto no nome ou descrição.
     */
    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchProjects(query: String): Flow<List<ProjectEntity>>
    
    /**
     * Obtém um projeto específico pelo ID.
     */
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: String): ProjectEntity?
    
    /**
     * Obtém projetos não sincronizados com o servidor.
     */
    @Query("SELECT * FROM projects WHERE isSynced = 0")
    suspend fun getUnsyncedProjects(): List<ProjectEntity>
    
    /**
     * Marca um projeto como sincronizado.
     */
    @Query("UPDATE projects SET isSynced = 1 WHERE id = :projectId")
    suspend fun markAsSynced(projectId: String)
    
    /**
     * Atualiza os valores totais de um projeto.
     */
    @Query("UPDATE projects SET totalPaid = :totalPaid, totalDue = :totalDue, lastUpdated = :timestamp WHERE id = :projectId")
    suspend fun updateProjectTotals(projectId: String, totalPaid: Double, totalDue: Double, timestamp: Long = System.currentTimeMillis())
} 