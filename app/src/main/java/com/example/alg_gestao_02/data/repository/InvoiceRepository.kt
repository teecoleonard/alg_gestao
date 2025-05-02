package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectInvoiceItem
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Repositório responsável por gerenciar os dados de faturas.
 * Esta classe implementa o padrão Repository do MVVM e centraliza
 * todas as operações relacionadas a dados de faturas.
 */
class InvoiceRepository {
    
    /**
     * Busca todas as faturas disponíveis.
     * Por enquanto usa dados mockados, mas no futuro chamará a API.
     */
    suspend fun getInvoices(): List<ProjectInvoiceItem> = withContext(Dispatchers.IO) {
        LogUtils.debug("InvoiceRepository", "Buscando todas as faturas")
        
        // Simula um delay de rede
        delay(500)
        
        // TODO: No futuro, chamar a API real aqui
        return@withContext getMockInvoices()
    }
    
    /**
     * Busca as faturas de um projeto específico.
     */
    suspend fun getInvoicesByProject(projectId: String): List<ProjectInvoiceItem> = withContext(Dispatchers.IO) {
        LogUtils.debug("InvoiceRepository", "Buscando faturas do projeto: $projectId")
        
        delay(300)
        
        // Filtra as faturas pelo ID do projeto
        return@withContext getMockInvoices().filter { it.projectId == projectId }
    }
    
    /**
     * Busca as faturas com um status específico.
     */
    suspend fun getInvoicesByStatus(status: String): List<ProjectInvoiceItem> = withContext(Dispatchers.IO) {
        LogUtils.debug("InvoiceRepository", "Buscando faturas com status: $status")
        
        delay(300)
        
        // Filtra as faturas pelo status
        return@withContext getMockInvoices().filter { it.status == status }
    }
    
    /**
     * Busca as faturas de um projeto com um status específico.
     */
    suspend fun getInvoicesByProjectAndStatus(projectId: String, status: String): List<ProjectInvoiceItem> = 
        withContext(Dispatchers.IO) {
            LogUtils.debug("InvoiceRepository", "Buscando faturas do projeto $projectId com status: $status")
            
            delay(300)
            
            // Filtra as faturas pelo ID do projeto e status
            return@withContext getMockInvoices().filter { 
                it.projectId == projectId && it.status == status 
            }
        }
    
    /**
     * Dados mockados para testes enquanto não integra com a API
     */
    private fun getMockInvoices(): List<ProjectInvoiceItem> {
        return listOf(
            ProjectInvoiceItem(
                id = "INV-001",
                numero = "NF-2023001",
                projectId = "1",
                valor = 8500.0,
                dataEmissao = "15/04/2023",
                dataVencimento = "15/05/2023",
                status = "paid"
            ),
            ProjectInvoiceItem(
                id = "INV-002",
                numero = "NF-2023002",
                projectId = "1",
                valor = 12000.0,
                dataEmissao = "20/05/2023",
                dataVencimento = "20/06/2023",
                status = "pending"
            ),
            ProjectInvoiceItem(
                id = "INV-003",
                numero = "NF-2023003",
                projectId = "2",
                valor = 7800.0,
                dataEmissao = "10/06/2023",
                dataVencimento = "10/07/2023",
                status = "overdue"
            ),
            ProjectInvoiceItem(
                id = "INV-004",
                numero = "NF-2023004",
                projectId = "2",
                valor = 15300.0,
                dataEmissao = "25/06/2023",
                dataVencimento = "25/07/2023",
                status = "cancelled"
            ),
            ProjectInvoiceItem(
                id = "INV-005",
                numero = "NF-2023005",
                projectId = "1",
                valor = 9200.0,
                dataEmissao = "05/07/2023",
                dataVencimento = "05/08/2023",
                status = "pending"
            )
        )
    }
} 