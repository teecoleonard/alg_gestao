package com.example.alg_gestao_02.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.alg_gestao_02.data.db.entity.ProjectInvoiceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para operações com a entidade ProjectInvoiceEntity.
 */
@Dao
interface ProjectInvoiceDao {
    
    /**
     * Insere uma fatura no banco de dados.
     * Em caso de conflito (mesmo ID), substitui o registro existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: ProjectInvoiceEntity)
    
    /**
     * Insere várias faturas no banco de dados.
     * Em caso de conflito (mesmo ID), substitui os registros existentes.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(invoices: List<ProjectInvoiceEntity>)
    
    /**
     * Atualiza uma fatura existente no banco de dados.
     */
    @Update
    suspend fun update(invoice: ProjectInvoiceEntity)
    
    /**
     * Remove uma fatura do banco de dados.
     */
    @Delete
    suspend fun delete(invoice: ProjectInvoiceEntity)
    
    /**
     * Remove uma fatura pelo ID.
     */
    @Query("DELETE FROM project_invoices WHERE id = :invoiceId")
    suspend fun deleteById(invoiceId: String)
    
    /**
     * Obtém todas as faturas de um projeto específico.
     * Retorna um Flow para receber atualizações em tempo real.
     */
    @Query("SELECT * FROM project_invoices WHERE projectId = :projectId ORDER BY dataEmissao DESC")
    fun getInvoicesByProjectId(projectId: String): Flow<List<ProjectInvoiceEntity>>
    
    /**
     * Obtém todas as faturas de um projeto com um status específico.
     */
    @Query("SELECT * FROM project_invoices WHERE projectId = :projectId AND status = :status ORDER BY dataEmissao DESC")
    fun getInvoicesByProjectIdAndStatus(projectId: String, status: String): Flow<List<ProjectInvoiceEntity>>
    
    /**
     * Obtém todas as faturas de um determinado mês e ano.
     * 
     * Nota: esta é uma implementação simplificada, supondo que dataEmissao seja uma string no formato "DD/MM/YYYY".
     * Para uma implementação mais robusta, considere armazenar as datas como timestamp ou usando um formato padrão.
     */
    @Query("SELECT * FROM project_invoices WHERE projectId = :projectId AND dataEmissao LIKE '%/' || :month || '/' || :year")
    fun getInvoicesByMonth(projectId: String, month: String, year: String): Flow<List<ProjectInvoiceEntity>>
    
    /**
     * Obtém faturas não sincronizadas com o servidor.
     */
    @Query("SELECT * FROM project_invoices WHERE isSynced = 0")
    suspend fun getUnsyncedInvoices(): List<ProjectInvoiceEntity>
    
    /**
     * Obtém uma fatura específica pelo ID.
     */
    @Query("SELECT * FROM project_invoices WHERE id = :invoiceId")
    suspend fun getInvoiceById(invoiceId: String): ProjectInvoiceEntity?
    
    /**
     * Marca uma fatura como sincronizada.
     */
    @Query("UPDATE project_invoices SET isSynced = 1 WHERE id = :invoiceId")
    suspend fun markAsSynced(invoiceId: String)
} 