package com.example.alg_gestao_02.ui.contract.repository

import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Repositório responsável por gerenciar dados de contratos.
 * Nota: Esta implementação simula acesso a dados. Em produção, isto se conectaria a uma API ou banco de dados.
 */
class ContractRepository {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("pt", "BR"))
    
    // Lista simulada de contratos para testes
    private val mockContracts = mutableListOf<ProjectContractItem>().apply {
        val names = listOf("Mohil Prajapati", "Freyja Hooper", "Alexander Gardner", "Aiden Schneider", "Eliana Acosta")
        val statuses = listOf("active", "pending", "inactive")
        val types = listOf("payment", "debt")
        val descriptions = listOf(
            "Contrato de prestação de serviços",
            "Acordo de desenvolvimento",
            "Termo de compromisso",
            "Contrato de manutenção",
            "Acordo para implementação de sistema"
        )
        
        val defaultProjectId = "1" // ID padrão para contratos mocados
        
        for (i in 1..10) {
            val today = LocalDate.now()
            val randomDays = Random.nextInt(1, 60)
            val date = today.minusDays(randomDays.toLong())
            val formattedDate = "${date.dayOfMonth} ${getMonthAbbreviation(date.monthValue)} ${date.year}, ${Random.nextInt(1, 12)}:${Random.nextInt(10, 59)} ${if (Random.nextBoolean()) "AM" else "PM"}"
            
            add(
                ProjectContractItem(
                    id = i.toString(),
                    projectId = defaultProjectId,
                    name = names[Random.nextInt(names.size)],
                    description = descriptions[Random.nextInt(descriptions.size)],
                    value = Random.nextDouble(10000.0, 2000000.0),
                    date = formattedDate,
                    status = statuses[Random.nextInt(statuses.size)],
                    type = types[Random.nextInt(types.size)]
                )
            )
        }
    }
    
    private fun getMonthAbbreviation(month: Int): String {
        return when (month) {
            1 -> "Jan"
            2 -> "Fev"
            3 -> "Mar"
            4 -> "Abr"
            5 -> "Mai"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Ago"
            9 -> "Set"
            10 -> "Out"
            11 -> "Nov"
            12 -> "Dez"
            else -> "Jan"
        }
    }
    
    /**
     * Obtém contratos por projeto e mês
     * @param projectId ID do projeto
     * @param month Mês (1-12)
     * @param year Ano
     * @return Lista de contratos
     */
    suspend fun getContractsByProjectAndMonth(
        projectId: Long,
        month: Int,
        year: Int
    ): List<ProjectContractItem> = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(800)
        
        mockContracts.shuffled().take(Random.nextInt(0, 6))
    }
    
    /**
     * Exclui um contrato pelo ID
     * @param contractId ID do contrato
     * @return true se a operação foi bem sucedida
     */
    suspend fun deleteContract(contractId: String): Boolean = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(500)
        
        val initialSize = mockContracts.size
        mockContracts.removeIf { it.id == contractId }
        
        initialSize > mockContracts.size
    }
    
    /**
     * Obtém todos os contratos para um projeto
     * @param projectId ID do projeto
     * @return Lista de contratos
     */
    suspend fun getAllContractsByProject(projectId: Long): List<ProjectContractItem> = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(1000)
        
        mockContracts
    }
    
    /**
     * Adiciona um novo contrato
     * @param contract Contrato a ser adicionado
     * @return O contrato adicionado com ID gerado
     */
    suspend fun addContract(contract: ProjectContractItem): ProjectContractItem = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(600)
        
        val newId = (mockContracts.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1
        val newContract = contract.copy(id = newId.toString())
        mockContracts.add(newContract)
        newContract
    }
    
    /**
     * Atualiza um contrato existente
     * @param contract Contrato com informações atualizadas
     * @return true se a operação foi bem sucedida
     */
    suspend fun updateContract(contract: ProjectContractItem): Boolean = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(600)
        
        val index = mockContracts.indexOfFirst { it.id == contract.id }
        if (index >= 0) {
            mockContracts[index] = contract
            true
        } else {
            false
        }
    }
} 