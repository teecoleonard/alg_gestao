package com.example.alg_gestao_02.ui.invoice.repository

import com.example.alg_gestao_02.data.models.FaturaProjeto
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Repositório para faturas na camada UI
 * Delega para a implementação de dados
 */
class InvoiceRepository {
    
    private val dataRepository = com.example.alg_gestao_02.data.repository.InvoiceRepository()
    
    /**
     * Busca todas as faturas disponíveis
     */
    suspend fun getInvoices(): List<FaturaProjeto> = withContext(Dispatchers.IO) {
        LogUtils.debug("UI InvoiceRepository", "Delegando busca de faturas para camada de dados")
        return@withContext dataRepository.getInvoices()
    }
    
    /**
     * Busca as faturas de um projeto específico
     */
    suspend fun getInvoicesByProject(projectId: String): List<FaturaProjeto> = withContext(Dispatchers.IO) {
        LogUtils.debug("UI InvoiceRepository", "Delegando busca de faturas por projeto para camada de dados")
        return@withContext dataRepository.getInvoicesByProject(projectId)
    }
    
    /**
     * Busca as faturas com um status específico
     */
    suspend fun getInvoicesByStatus(status: String): List<FaturaProjeto> = withContext(Dispatchers.IO) {
        LogUtils.debug("UI InvoiceRepository", "Delegando busca de faturas por status para camada de dados")
        return@withContext dataRepository.getInvoicesByStatus(status)
    }
    
    /**
     * Busca as faturas de um projeto com um status específico
     */
    suspend fun getInvoicesByProjectAndStatus(projectId: String, status: String): List<FaturaProjeto> = withContext(Dispatchers.IO) {
        LogUtils.debug("UI InvoiceRepository", "Delegando busca de faturas por projeto e status para camada de dados")
        return@withContext dataRepository.getInvoicesByProjectAndStatus(projectId, status)
    }
    
    /**
     * Busca as faturas de um projeto em um mês específico
     */
    suspend fun getInvoicesByProjectAndMonth(projectId: String, month: Int, year: Int): List<FaturaProjeto> = 
        withContext(Dispatchers.IO) {
            LogUtils.debug("UI InvoiceRepository", "Buscando faturas por projeto, mês e ano")
            
            // Simular pequeno delay de rede
            delay(300)
            
            // Como não temos uma implementação real, retornamos todas as faturas do projeto
            return@withContext dataRepository.getInvoicesByProject(projectId)
        }
    
    /**
     * Exclui uma fatura pelo ID
     */
    suspend fun deleteInvoice(invoiceId: String): Boolean = withContext(Dispatchers.IO) {
        LogUtils.debug("UI InvoiceRepository", "Excluindo fatura: $invoiceId")
        
        // Simular pequeno delay de rede
        delay(300)
        
        // Simulação de exclusão bem-sucedida
        return@withContext true
    }
    
    /**
     * Busca as faturas de um projeto em um mês específico com Calendar
     * Essa implementação filtra as faturas por mês, já que a implementação de dados não tem essa função
     */
    suspend fun getInvoicesByProjectAndMonth(projectId: String, month: Calendar): List<FaturaProjeto> = 
        withContext(Dispatchers.IO) {
            LogUtils.debug("UI InvoiceRepository", "Buscando faturas por projeto e mês (Calendar)")
            
            val invoices = dataRepository.getInvoicesByProject(projectId)
            
            // Filtrar por mês seria feito aqui se tivéssemos datas reais
            // Por enquanto retornamos todas as faturas
            return@withContext invoices
        }
} 