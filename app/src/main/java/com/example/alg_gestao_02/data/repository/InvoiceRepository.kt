package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.models.FaturaProjeto
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Repositório para acesso a dados de faturas.
 */
class InvoiceRepository {
    
    /**
     * Busca todas as faturas
     */
    suspend fun getInvoices(): List<FaturaProjeto> = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(500)
        
        // Retorna dados mockados
        return@withContext getMockInvoices()
    }
    
    /**
     * Busca faturas por projeto
     */
    suspend fun getInvoicesByProject(projectId: String): List<FaturaProjeto> = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(500)
        
        // Filtra as faturas pelo projeto
        return@withContext getMockInvoices().filter { it.projectId == projectId }
    }
    
    /**
     * Busca faturas por status
     */
    suspend fun getInvoicesByStatus(status: String): List<FaturaProjeto> = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(500)
        
        // Filtra as faturas pelo status
        return@withContext getMockInvoices().filter { it.status == status }
    }
    
    /**
     * Busca faturas por projeto e status
     */
    suspend fun getInvoicesByProjectAndStatus(projectId: String, status: String): List<FaturaProjeto> =
        withContext(Dispatchers.IO) {
            // Simula uma operação de rede
            delay(500)
            
            // Filtra as faturas pelo projeto e status
            return@withContext getMockInvoices().filter { it.projectId == projectId && it.status == status }
        }
    
    /**
     * Lista mockada de faturas para fins de demonstração
     */
    private fun getMockInvoices(): List<FaturaProjeto> {
        return listOf(
            FaturaProjeto(
                id = "1",
                projectId = "1",
                title = "Janeiro 2024",
                description = "Fatura mensal de janeiro",
                value = "R$ 12.500,00",
                date = "15/01/2024",
                dueDate = "25/01/2024",
                status = "paid",
                month = 1,
                year = 2024
            ),
            FaturaProjeto(
                id = "2",
                projectId = "1",
                title = "Fevereiro 2024",
                description = "Fatura mensal de fevereiro",
                value = "R$ 12.500,00",
                date = "15/02/2024",
                dueDate = "25/02/2024",
                status = "paid",
                month = 2,
                year = 2024
            ),
            FaturaProjeto(
                id = "3",
                projectId = "1",
                title = "Março 2024",
                description = "Fatura mensal de março",
                value = "R$ 12.500,00",
                date = "15/03/2024",
                dueDate = "25/03/2024",
                status = "paid",
                month = 3,
                year = 2024
            ),
            FaturaProjeto(
                id = "4",
                projectId = "1",
                title = "Abril 2024",
                description = "Fatura mensal de abril",
                value = "R$ 12.500,00",
                date = "15/04/2024",
                dueDate = "25/04/2024",
                status = "pending",
                month = 4,
                year = 2024
            ),
            FaturaProjeto(
                id = "5",
                projectId = "1",
                title = "Maio 2024",
                description = "Fatura mensal de maio",
                value = "R$ 12.500,00",
                date = "15/05/2024",
                dueDate = "25/05/2024",
                status = "overdue",
                month = 5,
                year = 2024
            )
        )
    }
} 